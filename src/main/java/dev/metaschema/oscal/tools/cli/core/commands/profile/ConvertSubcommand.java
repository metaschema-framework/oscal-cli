/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.profile;

import dev.metaschema.core.model.IBoundObject;
import dev.metaschema.oscal.lib.model.Profile;
import dev.metaschema.oscal.tools.cli.core.commands.AbstractOscalConvertCommand;

class ConvertSubcommand
    extends AbstractOscalConvertCommand {
  @Override
  public String getDescription() {
    return "Convert a specified OSCAL Profile to a different format";
  }

  @Override
  public Class<? extends IBoundObject> getOscalClass() {
    return Profile.class;
  }
}
