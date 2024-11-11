/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.metaschema;

import gov.nist.secauto.metaschema.cli.commands.MetaschemaCommands;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractParentCommand;

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
    super(true);
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
