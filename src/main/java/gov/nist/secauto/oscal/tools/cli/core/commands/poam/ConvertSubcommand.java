/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.poam;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.oscal.lib.model.PlanOfActionAndMilestones;
import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractOscalConvertSubcommand;

public class ConvertSubcommand
    extends AbstractOscalConvertSubcommand {
  @Override
  public String getDescription() {
    return "Convert the specified OSCAL Plan of Actions and Milestones to a different format";
  }

  @Override
  public Class<? extends IBoundObject> getOscalClass() {
    return PlanOfActionAndMilestones.class;
  }
}
