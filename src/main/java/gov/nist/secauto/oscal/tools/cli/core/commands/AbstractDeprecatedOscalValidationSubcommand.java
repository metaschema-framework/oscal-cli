/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.processor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.command.CommandExecutionException;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This abstract command implementation provides user feedback about extending
 * command being deprecated in favor of the {@link ValidateCommand}.
 */
public abstract class AbstractDeprecatedOscalValidationSubcommand
    extends AbstractOscalValidationCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractDeprecatedOscalValidationSubcommand.class);

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new CommandExecutor(callingContext, commandLine);
  }

  private final class CommandExecutor
      extends AbstractOscalValidationCommand.OscalValidationCommandExecutor {

    private CommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    public void execute() throws CommandExecutionException {
      LOGGER.atWarn().log("This command path is deprecated. Please use 'validate'.");

      super.execute();
    }
  }
}
