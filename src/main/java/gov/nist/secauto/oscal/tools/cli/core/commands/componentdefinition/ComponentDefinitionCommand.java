/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.componentdefinition;

import gov.nist.secauto.metaschema.cli.processor.command.AbstractParentCommand;

/**
 * A parent command implementation that organizes commands related to an OSCAL
 * component definition.
 */
public class ComponentDefinitionCommand
    extends AbstractParentCommand {
  private static final String COMMAND = "component-definition";

  /**
   * Construct a new parent command.
   */
  public ComponentDefinitionCommand() {
    super(true);
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
    return "Perform an operation on an OSCAL Component Definition";
  }
}
