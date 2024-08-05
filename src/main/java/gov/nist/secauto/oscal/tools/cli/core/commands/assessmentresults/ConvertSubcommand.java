/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.assessmentresults;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractOscalConvertSubcommand;

public class ConvertSubcommand
    extends AbstractOscalConvertSubcommand {
  @Override
  public String getDescription() {
    return "Convert the specified OSCAL Assessment Results to a different format";
  }

  @Override
  public Class<? extends IBoundObject> getOscalClass() {
    return AssessmentResults.class;
  }
}
