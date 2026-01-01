/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.metaschema.cli.processor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.command.CommandExecutionException;
import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractResolveCommand;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;

class ResolveSubcommand
    extends AbstractResolveCommand {
  private static final Logger LOGGER = LogManager.getLogger(ResolveSubcommand.class);

  @NonNull
  private static final String COMMAND = "resolve";

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  protected void executeCommand(CallingContext callingContext, CommandLine cmdLine) throws CommandExecutionException {
    LOGGER.atWarn().log("This command path is deprecated. Please use 'resolve-profile'.");

    super.executeCommand(callingContext, cmdLine);
  }
}
