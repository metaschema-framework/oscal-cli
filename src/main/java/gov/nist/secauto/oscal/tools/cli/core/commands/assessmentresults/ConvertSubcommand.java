/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.assessmentresults;

import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;
import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractOscalConvertCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.ConvertCommand;

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
    return "Convert the specified OSCAL Assessment Results to a different format";
  }

  @Override
  public Class<? extends IBoundObject> getOscalClass() {
    return AssessmentResults.class;
  }
}
