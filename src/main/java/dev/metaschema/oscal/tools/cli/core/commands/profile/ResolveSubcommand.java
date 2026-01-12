/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.profile;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.metaschema.cli.processor.CallingContext;
import dev.metaschema.cli.processor.command.CommandExecutionException;
import dev.metaschema.oscal.tools.cli.core.commands.AbstractResolveCommand;
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
