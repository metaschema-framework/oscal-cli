/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractRenderSubcommand;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.transform.TransformerException;

public class RenderSubcommand
    extends AbstractRenderSubcommand {
  @Override
  public String getDescription() {
    return "Render the specified OSCAL Profile as HTML";
  }

  @Override
  protected void performRender(Path input, Path result) throws IOException, TransformerException {
    XMLOperations.renderProfileHTML(input.toFile(), result.toFile());
  }
}
