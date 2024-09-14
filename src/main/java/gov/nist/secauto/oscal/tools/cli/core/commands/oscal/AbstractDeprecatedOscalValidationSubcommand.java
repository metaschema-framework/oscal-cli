/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.oscal;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractOscalValidationCommand;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractDeprecatedOscalValidationSubcommand
    extends AbstractOscalValidationCommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractDeprecatedOscalValidationSubcommand.class);

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new DeprecatedOscalCommandExecutor(callingContext, commandLine);
  }

  protected final class DeprecatedOscalCommandExecutor
      extends AbstractOscalValidationCommand.OscalCommandExecutor {

    private DeprecatedOscalCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    public ExitStatus execute() {
      LOGGER.atWarn().log("This command path is deprecated. Please use 'validate'.");

      return super.execute();
    }
  }
}
