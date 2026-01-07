/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.assessmentresults;

import dev.metaschema.cli.processor.command.AbstractParentCommand;

/**
 * A parent command implementation that organizes commands related to an OSCAL
 * assessment results.
 */
public class AssessmentResultsCommand
    extends AbstractParentCommand {
  private static final String COMMAND = "ar";

  /**
   * Construct a new parent command.
   */
  public AssessmentResultsCommand() {
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
    return "Perform an operation on an OSCAL Assessment Results";
  }
}
