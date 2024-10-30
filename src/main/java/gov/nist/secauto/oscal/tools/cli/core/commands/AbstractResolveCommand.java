/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.InvalidArgumentException;
import gov.nist.secauto.metaschema.cli.processor.OptionUtils;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.CustomCollectors;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.Profile;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractResolveCommand
    extends AbstractTerminalCommand {
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("file to resolve", true),
      new DefaultExtraArgument("destination file", false)));
  @NonNull
  private static final Option AS_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("as")
          .hasArg()
          .argName("FORMAT")
          .desc("source format: xml, json, or yaml")
          .build());
  @NonNull
  private static final Option TO_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("to")
          .required()
          .hasArg().argName("FORMAT")
          .desc("convert to format: xml, json, or yaml")
          .build());
  @NonNull
  private static final Option OVERWRITE_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("overwrite")
          .desc("overwrite the destination if it exists")
          .build());
  @NonNull
  private static final List<Option> OPTIONS = ObjectUtils.notNull(
      List.of(
          AS_OPTION,
          TO_OPTION,
          OVERWRITE_OPTION));

  @Override
  public String getDescription() {
    return "Resolve the specified OSCAL Profile";
  }

  @Override
  public Collection<? extends Option> gatherOptions() {
    return OPTIONS;
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @SuppressWarnings({
      "PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", // reasonable
      "PMD.PreserveStackTrace" // intended
  })
  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    if (cmdLine.hasOption(AS_OPTION)) {
      try {
        String toFormatText = cmdLine.getOptionValue(AS_OPTION);
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        InvalidArgumentException newEx = new InvalidArgumentException(
            String.format("Invalid '%s' argument. The format must be one of: %s.",
                OptionUtils.toArgument(AS_OPTION),
                Arrays.asList(Format.values()).stream()
                    .map(Enum::name)
                    .collect(CustomCollectors.joiningWithOxfordComma("and"))));
        newEx.setOption(AS_OPTION);
        newEx.addSuppressed(ex);
        throw newEx;
      }
    }

    if (cmdLine.hasOption(TO_OPTION)) {
      try {
        String toFormatText = cmdLine.getOptionValue(TO_OPTION);
        Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        InvalidArgumentException newEx
            = new InvalidArgumentException("Invalid '--to' argument. The format must be one of: "
                + Arrays.asList(Format.values()).stream()
                    .map(Enum::name)
                    .collect(CustomCollectors.joiningWithOxfordComma("and")));
        newEx.setOption(AS_OPTION);
        newEx.addSuppressed(ex);
        throw newEx;
      }
    }

    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.isEmpty()) {
      throw new InvalidArgumentException("The source to resolve must be provided.");
    }

    File source = new File(extraArgs.get(0));
    if (!source.exists()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' does not exist.");
    }
    if (!source.canRead()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' is not readable.");
    }
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "unused"
  })
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) {
    List<String> extraArgs = cmdLine.getArgList();
    Path source = resolvePathAgainstCWD(ObjectUtils.notNull(Paths.get(extraArgs.get(0))));

    IBindingContext bindingContext = OscalBindingContext.instance();
    IBoundLoader loader = bindingContext.newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    Format asFormat;
    // attempt to determine the format
    if (cmdLine.hasOption(AS_OPTION)) {
      try {
        String asFormatText = cmdLine.getOptionValue(AS_OPTION);
        asFormat = Format.valueOf(asFormatText.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        return ExitCode.INVALID_ARGUMENTS
            .exitMessage("Invalid '--as' argument. The format must be one of: " + Arrays.stream(Format.values())
                .map(Enum::name)
                .collect(CustomCollectors.joiningWithOxfordComma("or")));
      }
    } else {
      // attempt to determine the format
      try {
        asFormat = loader.detectFormat(ObjectUtils.notNull(source));
      } catch (FileNotFoundException ex) {
        // this case was already checked for
        return ExitCode.IO_ERROR.exitMessage("The provided source file '" + source + "' does not exist.");
      } catch (IOException ex) {
        return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
      } catch (IllegalArgumentException ex) {
        return ExitCode.INVALID_ARGUMENTS.exitMessage(
            "Source file has unrecognizable format. Use '--as' to specify the format. The format must be one of: "
                + Arrays.stream(Format.values())
                    .map(Enum::name)
                    .collect(CustomCollectors.joiningWithOxfordComma("or")));
      }
    }

    source = source.toAbsolutePath();
    assert source != null;

    Format toFormat;
    if (cmdLine.hasOption(TO_OPTION)) {
      String toFormatText = cmdLine.getOptionValue(TO_OPTION);
      toFormat = Format.valueOf(toFormatText.toUpperCase(Locale.ROOT));
    } else {
      toFormat = asFormat;
    }

    Path destination = null;
    if (extraArgs.size() == 2) {
      destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
    }

    if (destination != null) {
      if (Files.exists(destination)) {
        if (!cmdLine.hasOption(OVERWRITE_OPTION)) {
          return ExitCode.INVALID_ARGUMENTS.exitMessage("The provided destination '" + destination
              + "' already exists and the --overwrite option was not provided.");
        }
        if (!Files.isWritable(destination)) {
          return ExitCode.IO_ERROR.exitMessage("The provided destination '" + destination + "' is not writable.");
        }
      } else {
        Path parent = destination.getParent();
        if (parent != null) {
          try {
            Files.createDirectories(parent);
          } catch (IOException ex) {
            return ExitCode.INVALID_TARGET.exit().withThrowable(ex);
          }
        }
      }
    }

    IDocumentNodeItem document;
    try {
      document = loader.loadAsNodeItem(asFormat, source);
    } catch (IOException ex) {
      return ExitCode.IO_ERROR.exit().withThrowable(ex);
    }
    Object object = document.getValue();
    if (object == null) {
      return ExitCode.INVALID_ARGUMENTS.exitMessage("The target profile contained no data");
    }

    if (object instanceof Catalog) {
      // this is a catalog
      return ExitCode.INVALID_ARGUMENTS.exitMessage("The target is already a catalog");
    }

    if (!(object instanceof Profile)) {
      // this is something else
      return ExitCode.INVALID_ARGUMENTS.exitMessage("The target is not a profile");
    }

    // this is a profile
    DynamicContext dynamicContext = new DynamicContext(document.getStaticContext());
    dynamicContext.setDocumentLoader(loader);
    ProfileResolver resolver = new ProfileResolver(dynamicContext);

    IDocumentNodeItem resolvedProfile;
    try {
      resolvedProfile = resolver.resolve(document);
    } catch (IOException | ProfileResolutionException ex) {
      return ExitCode.PROCESSING_ERROR
          .exitMessage(
              String.format("Cmd: Unable to resolve profile '%s'. %s", document.getDocumentUri(), ex.getMessage()))
          .withThrowable(ex);
    }

    // DefaultConstraintValidator validator = new
    // DefaultConstraintValidator(dynamicContext);
    // ((IBoundXdmNodeItem)resolvedProfile).validate(validator);
    // validator.finalizeValidation();

    ISerializer<Catalog> serializer = bindingContext.newSerializer(toFormat, Catalog.class);
    try {
      if (destination == null) {
        @SuppressWarnings({ "resource", "PMD.CloseResource" }) PrintStream stdOut = ObjectUtils.notNull(System.out);
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), stdOut);
      } else {
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), destination);
      }
    } catch (IOException ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
    }
    return ExitCode.OK.exit();
  }
}
