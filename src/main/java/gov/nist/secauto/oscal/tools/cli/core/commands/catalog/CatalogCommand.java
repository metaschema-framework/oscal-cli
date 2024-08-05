/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.catalog;

import com.google.auto.service.AutoService;

import gov.nist.secauto.metaschema.cli.processor.command.AbstractParentCommand;
import gov.nist.secauto.metaschema.cli.processor.command.ICommand;

@AutoService(ICommand.class)
public class CatalogCommand
    extends AbstractParentCommand {
  private static final String COMMAND = "catalog";

  public CatalogCommand() {
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
    return "Perform an operation on an OSCAL Catalog";
  }

}
