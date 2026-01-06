/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.componentdefinition;

import dev.metaschema.core.model.IBoundObject;
import dev.metaschema.oscal.lib.model.ComponentDefinition;
import dev.metaschema.oscal.tools.cli.core.commands.AbstractOscalConvertCommand;
import dev.metaschema.oscal.tools.cli.core.commands.ConvertCommand;

/**
 * Provides an OSCAL content conversion command.
 * <p>
 * This executor provides user feedback stating that this command is deprecated
 * in favor of the {@link ConvertCommand}.
 */
public class ConvertSubcommand
    extends AbstractOscalConvertCommand {
  @Override
  public String getDescription() {
    return "Convert the specified OSCAL Component Definition to a different format";
  }

  @Override
  public Class<? extends IBoundObject> getOscalClass() {
    return ComponentDefinition.class;
  }
}
