/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.catalog;

import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractRenderCommand;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.transform.TransformerException;

public class RenderSubcommand
    extends AbstractRenderCommand {
  @Override
  public String getDescription() {
    return "Render the specified OSCAL Catalog as HTML";
  }

  @Override
  protected void performRender(Path input, Path result) throws IOException, TransformerException {
    XMLOperations.renderCatalogHTML(input.toFile(), result.toFile());
  }
}
