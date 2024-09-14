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
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.TransformerException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.size() != 2) {
      throw new InvalidArgumentException("Both a source and destination argument must be provided.");
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
    Path destination = resolvePathAgainstCWD(ObjectUtils.notNull(Paths.get(extraArgs.get(1)))); // .toAbsolutePath();

    if (Files.exists(destination)) {
      if (!cmdLine.hasOption(OVERWRITE_OPTION)) {
        return ExitCode.INVALID_ARGUMENTS.exitMessage(
            String.format("The provided destination '%s' already exists and the '%s' option was not provided.",
                destination,
                OptionUtils.toArgument(OVERWRITE_OPTION)));
      }
      if (!Files.isWritable(destination)) {
        return ExitCode.IO_ERROR.exitMessage("The provided destination '" + destination + "' is not writable.");
      }
    }

    Path input = resolvePathAgainstCWD(ObjectUtils.notNull(Paths.get(extraArgs.get(0))));
    try {
      performRender(input, destination);
    } catch (IOException | TransformerException ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Generated HTML file: " + destination.toString());
    }
    return ExitCode.OK.exit();
  }

  protected abstract void performRender(Path input, Path result) throws IOException, TransformerException;
}
