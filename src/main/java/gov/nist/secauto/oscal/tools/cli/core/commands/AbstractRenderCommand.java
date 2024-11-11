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
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.TransformerException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * An abstract command implementation supporting the rendering of an OSCAL
 * content instance.
 */
public abstract class AbstractRenderCommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractRenderCommand.class);

  @NonNull
  private static final String COMMAND = "render";
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
      new DefaultExtraArgument("source file", true),
      new DefaultExtraArgument("destination file", false)));

  @NonNull
  private static final Option OVERWRITE_OPTION = ObjectUtils.notNull(
      Option.builder()
          .longOpt("overwrite")
          .desc("overwrite the destination if it exists")
          .build());

  @Override
  public String getName() {
    return COMMAND;
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends Option> gatherOptions() {
    return List.of(OVERWRITE_OPTION);
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "unmodifiable collection and immutable item")
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  /**
   * Process the command line arguments and execute the rendering operation.
   *
   * @param callingContext
   *          the context information for the execution
   * @param cmdLine
   *          the parsed command line details
   * @throws CommandExecutionException
   *           if an error occurred while determining the source format
   */
  @SuppressWarnings({
      "PMD.OnlyOneReturn" // readability
  })
  protected void executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) throws CommandExecutionException {
    List<String> extraArgs = cmdLine.getArgList();
    Path destination = null;
    if (extraArgs.size() > 1) {
      destination = MetaschemaCommands.handleDestination(ObjectUtils.requireNonNull(extraArgs.get(1)), cmdLine);
    }

    URI source = MetaschemaCommands.handleSource(
        ObjectUtils.requireNonNull(extraArgs.get(0)),
        ObjectUtils.notNull(getCurrentWorkingDirectory().toUri()));

    try {
      performRender(source, destination);
    } catch (IOException ex) {
      throw new CommandExecutionException(ExitCode.IO_ERROR, ex);
    } catch (TransformerException ex) {
      throw new CommandExecutionException(ExitCode.PROCESSING_ERROR, ex);
    }

    if (destination != null && LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated HTML file: " + destination.toString());
    }
  }

  /**
   * Perform the rendering operation.
   *
   * @param input
   *          the OSCAL content resource to render
   * @param result
   *          the destination file to create rendered content in
   * @throws IOException
   *           if an error occurred while loading the input
   * @throws TransformerException
   *           if an error occurred while creating the rendered content
   */
  protected abstract void performRender(@NonNull URI input, Path result)
      throws IOException, TransformerException;
}
