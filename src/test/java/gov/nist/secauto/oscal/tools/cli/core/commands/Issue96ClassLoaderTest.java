/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Issue96ClassLoaderTest {
  /**
   * Regression tests for usnistgov/oscal-cli#96. See information at this URL for
   * more details. https://github.com/usnistgov/oscal-cli/issues/96
   */
  @Test
  void testAssessmentPlanClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.assessmentplan.ConvertSubcommand();
    assertEquals("AssessmentPlan", subcommand.getOscalClass().getSimpleName());
  }

  @Test
  void testAssessmentResultsClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.assessmentresults.ConvertSubcommand();
    assertEquals("AssessmentResults", subcommand.getOscalClass().getSimpleName());
  }

  @Test
  void testCatalogClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.catalog.ConvertSubcommand();
    assertEquals("Catalog", subcommand.getOscalClass().getSimpleName());
  }

  @Test
  void testPoamClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.poam.ConvertSubcommand();
    assertEquals("PlanOfActionAndMilestones", subcommand.getOscalClass().getSimpleName());
  }

  @Test
  void testSspClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.ssp.ConvertSubcommand();
    assertEquals("SystemSecurityPlan", subcommand.getOscalClass().getSimpleName());
  }

  @Test
  void testComponentDefinitionClassLoader() {
    AbstractOscalConvertCommand subcommand
        = new gov.nist.secauto.oscal.tools.cli.core.commands.componentdefinition.ConvertSubcommand();
    assertEquals("ComponentDefinition", subcommand.getOscalClass().getSimpleName());
  }
}
