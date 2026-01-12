/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import dev.metaschema.cli.commands.MetaschemaCommands;
import dev.metaschema.cli.processor.CallingContext;
import dev.metaschema.cli.processor.ExitCode;
import dev.metaschema.cli.processor.command.AbstractTerminalCommand;
import dev.metaschema.cli.processor.command.CommandExecutionException;
import dev.metaschema.cli.processor.command.ExtraArgument;
import dev.metaschema.cli.processor.command.ICommandExecutor;
import dev.metaschema.core.metapath.DynamicContext;
import dev.metaschema.core.metapath.item.node.IDocumentNodeItem;
import dev.metaschema.core.metapath.item.node.INodeItem;
import dev.metaschema.core.util.ObjectUtils;
import dev.metaschema.core.util.UriUtils;
import dev.metaschema.databind.IBindingContext;
import dev.metaschema.databind.io.DeserializationFeature;
import dev.metaschema.databind.io.Format;
import dev.metaschema.databind.io.IBoundLoader;
import dev.metaschema.databind.io.ISerializer;
import dev.metaschema.oscal.lib.OscalBindingContext;
import dev.metaschema.oscal.lib.model.Catalog;
import dev.metaschema.oscal.lib.model.Profile;
import dev.metaschema.oscal.lib.profile.resolver.ProfileResolutionException;
import dev.metaschema.oscal.lib.profile.resolver.ProfileResolver;
import dev.metaschema.oscal.tools.cli.core.utils.PrettyPrinter;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A command implementation supporting the resolution of an OSCAL profile.
 */
public abstract class AbstractResolveCommand
    extends AbstractTerminalCommand {
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      ExtraArgument.newInstance("URI to resolve", true),
      ExtraArgument.newInstance("destination file", false)));
  private static final Option RELATIVE_TO = Option.builder()
      .longOpt("relative-to")
      .desc("Generate URI references relative to this resource")
      .hasArg()
      .build();
  private static final Option PRETTY_PRINT_OPTION = Option.builder()
      .longOpt("pretty-print")
      .desc("Enable pretty-printing of the output for better readability")
      .build();

  @NonNull
  private static final List<Option> OPTIONS = ObjectUtils.notNull(
      List.of(
          MetaschemaCommands.AS_FORMAT_OPTION,
          MetaschemaCommands.TO_OPTION,
          MetaschemaCommands.OVERWRITE_OPTION,
          RELATIVE_TO,
          PRETTY_PRINT_OPTION));

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
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  /**
   * Process the command line arguments and execute the profile resolution
   * operation.
   *
   * @param callingContext
   *          the context information for the execution
   * @param cmdLine
   *          the parsed command line details
   * @throws CommandExecutionException
   *           if an error occurred while determining the source format
   */
  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "PMD.CyclomaticComplexity"
  })
  protected void executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) throws CommandExecutionException {
    List<String> extraArgs = cmdLine.getArgList();

    URI source = MetaschemaCommands.handleSource(
        ObjectUtils.requireNonNull(extraArgs.get(0)),
        ObjectUtils.notNull(getCurrentWorkingDirectory().toUri()));

    IBindingContext bindingContext = OscalBindingContext.instance();
    IBoundLoader loader = bindingContext.newBoundLoader();
    loader.disableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);

    // attempt to determine the format
    Format asFormat = MetaschemaCommands.determineSourceFormat(
        cmdLine,
        MetaschemaCommands.AS_FORMAT_OPTION,
        loader,
        source);

    IDocumentNodeItem document;
    try {
      document = loader.loadAsNodeItem(asFormat, source);
    } catch (IOException ex) {
      throw new CommandExecutionException(
          ExitCode.IO_ERROR,
          String.format("Unable to load content '%s'. %s",
              source,
              ex.getMessage()),
          ex);
    }

    Object object = document.getValue();
    if (object == null) {
      throw new CommandExecutionException(
          ExitCode.INVALID_ARGUMENTS,
          String.format("The source document '%s' contained no data.", source));
    }

    if (object instanceof Catalog) {
      // this is a catalog
      throw new CommandExecutionException(
          ExitCode.OK,
          String.format("The source '%s' is already a catalog.", source));
    }

    if (!(object instanceof Profile)) {
      // this is something else
      throw new CommandExecutionException(
          ExitCode.INVALID_ARGUMENTS,
          String.format("The source '%s' is not a profile.", source));
    }

    Path destination = null;
    if (extraArgs.size() > 1) {
      destination = MetaschemaCommands.handleDestination(ObjectUtils.requireNonNull(extraArgs.get(1)), cmdLine);
    }

    URI relativeTo;
    if (cmdLine.hasOption(RELATIVE_TO)) {
      relativeTo = getCurrentWorkingDirectory().toUri().resolve(cmdLine.getOptionValue(RELATIVE_TO));
    } else {
      relativeTo = document.getDocumentUri();
    }

    // this is a profile
    DynamicContext dynamicContext = new DynamicContext(document.getStaticContext());
    dynamicContext.setDocumentLoader(loader);
    ProfileResolver resolver = new ProfileResolver(
        dynamicContext,
        (uri, src) -> {
          try {
            return UriUtils.relativize(relativeTo, src.resolve(uri), true);
          } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
          }
        });

    IDocumentNodeItem resolvedProfile;
    try {
      resolvedProfile = resolver.resolve(document);
    } catch (IOException | ProfileResolutionException ex) {
      throw new CommandExecutionException(
          ExitCode.PROCESSING_ERROR,
          String.format("Cmd: Unable to resolve profile '%s'. %s", document.getDocumentUri(), ex.getMessage()),
          ex);
    }

    // DefaultConstraintValidator validator = new
    // DefaultConstraintValidator(dynamicContext);
    // ((IBoundXdmNodeItem)resolvedProfile).validate(validator);
    // validator.finalizeValidation();

    Format toFormat = MetaschemaCommands.getFormat(cmdLine, MetaschemaCommands.TO_OPTION);
    boolean prettyPrint = cmdLine.hasOption(PRETTY_PRINT_OPTION);
    ISerializer<Catalog> serializer = bindingContext.newSerializer(toFormat, Catalog.class);
    try {
      if (destination == null) {
        @SuppressWarnings({ "resource", "PMD.CloseResource" })
        PrintStream stdOut = ObjectUtils.notNull(System.out);
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), stdOut);
      } else {
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), destination);
        if (prettyPrint) {
          prettyPrintOutput(destination, toFormat);
        }
      }
    } catch (IOException ex) {
      throw new CommandExecutionException(ExitCode.IO_ERROR, ex);
    }
  }

  /**
   * Pretty-print the output file based on the specified format.
   * <p>
   * This feature was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
   * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
   * </p>
   *
   * @param destination
   *          the path to the output file
   * @param toFormat
   *          the format of the output file
   * @throws CommandExecutionException
   *           if pretty-printing fails
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private void prettyPrintOutput(@NonNull Path destination, @NonNull Format toFormat)
      throws CommandExecutionException {
    try {
      switch (toFormat) {
      case JSON:
        PrettyPrinter.prettyPrintJson(destination.toFile());
        break;
      case YAML:
        PrettyPrinter.prettyPrintYaml(destination.toFile());
        break;
      case XML:
        PrettyPrinter.prettyPrintXml(destination.toFile());
        break;
      default:
        // do nothing for unknown formats
        break;
      }
    } catch (Exception ex) {
      throw new CommandExecutionException(
          ExitCode.PROCESSING_ERROR,
          String.format("Pretty-printing failed: %s", ex.getMessage()),
          ex);
    }
  }
}
