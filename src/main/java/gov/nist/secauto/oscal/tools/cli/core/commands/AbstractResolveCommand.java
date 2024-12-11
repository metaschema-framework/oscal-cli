/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.commands.MetaschemaCommands;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.CommandExecutionException;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.core.util.UriUtils;
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

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

  @NonNull
  private static final List<Option> OPTIONS = ObjectUtils.notNull(
      List.of(
          MetaschemaCommands.AS_FORMAT_OPTION,
          MetaschemaCommands.TO_OPTION,
          MetaschemaCommands.OVERWRITE_OPTION,
          RELATIVE_TO));

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
    ISerializer<Catalog> serializer = bindingContext.newSerializer(toFormat, Catalog.class);
    try {
      if (destination == null) {
        @SuppressWarnings({ "resource", "PMD.CloseResource" })
        PrintStream stdOut = ObjectUtils.notNull(System.out);
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), stdOut);
      } else {
        serializer.serialize((Catalog) INodeItem.toValue(resolvedProfile), destination);
      }
    } catch (IOException ex) {
      throw new CommandExecutionException(ExitCode.IO_ERROR, ex);
    }
  }
}
