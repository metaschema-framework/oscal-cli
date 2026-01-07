/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.metaschema;

import dev.metaschema.cli.commands.MetaschemaCommands;
import dev.metaschema.cli.processor.command.AbstractParentCommand;

/**
 * A parent command implementation that organizes commands related to Metaschema
 * operations.
 */
public class MetaschemaCommand
    extends AbstractParentCommand {
  private static final String COMMAND = "metaschema";

  /**
   * Construct a new parent command.
   */
  public MetaschemaCommand() {
    MetaschemaCommands.COMMANDS.forEach(this::addCommandHandler);
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Perform an operation on a Module";
  }
}
