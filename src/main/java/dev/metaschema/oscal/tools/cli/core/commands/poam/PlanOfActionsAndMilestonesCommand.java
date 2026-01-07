/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.poam;

import dev.metaschema.cli.processor.command.AbstractParentCommand;

/**
 * A parent command implementation that organizes commands related to an OSCAL
 * plan of actions and milestone.
 */
public class PlanOfActionsAndMilestonesCommand
    extends AbstractParentCommand {
  private static final String COMMAND = "poam";

  /**
   * Construct a new parent command.
   */
  public PlanOfActionsAndMilestonesCommand() {
    addCommandHandler(new ValidateSubcommand());
    // addCommandHandler(new RenderSubcommand());
    addCommandHandler(new ConvertSubcommand());
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Perform an operation on an OSCAL Plan of Actions and Milestones";
  }
}
