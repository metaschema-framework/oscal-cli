/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core;

import gov.nist.secauto.metaschema.cli.processor.CLIProcessor;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.core.MetaschemaJavaVersion;
import gov.nist.secauto.metaschema.core.model.MetaschemaVersion;
import gov.nist.secauto.metaschema.core.util.IVersionInfo;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.LibOscalVersion;
import gov.nist.secauto.oscal.lib.OscalVersion;
import gov.nist.secauto.oscal.tools.cli.core.commands.ConvertCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.ListAllowedValuesCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.ResolveCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.ValidateCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.assessmentplan.AssessmentPlanCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.assessmentresults.AssessmentResultsCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.catalog.CatalogCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.componentdefinition.ComponentDefinitionCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.metaschema.MetaschemaCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.poam.PlanOfActionsAndMilestonesCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.profile.ProfileCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.ssp.SystemSecurityPlanCommand;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("PMD.ShortClassName")
public final class CLI {
  public static void main(String[] args) {
    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

    ExitStatus status = runCli(args);
    int exitCode = status.getExitCode().getStatusCode();
    System.exit(exitCode);
  }

  @NonNull
  public static ExitStatus runCli(String... args) {
    @SuppressWarnings("serial") Map<String, IVersionInfo> versions = ObjectUtils.notNull(
        new LinkedHashMap<>() {
          {
            put(CLIProcessor.COMMAND_VERSION, new OscalCliVersion());
            put("https://github.com/usnistgov/liboscal-java", new LibOscalVersion());
            put("https://github.com/usnistgov/OSCAL", new OscalVersion());
            put("https://github.com/usnistgov/metaschema-java", new MetaschemaJavaVersion());
            put("https://github.com/usnistgov/metaschema", new MetaschemaVersion());
          }
        });
    CLIProcessor processor = new CLIProcessor("oscal-cli", versions);
    processor.addCommandHandler(new CatalogCommand());
    processor.addCommandHandler(new ProfileCommand());
    processor.addCommandHandler(new ComponentDefinitionCommand());
    processor.addCommandHandler(new SystemSecurityPlanCommand());
    processor.addCommandHandler(new AssessmentPlanCommand());
    processor.addCommandHandler(new AssessmentResultsCommand());
    processor.addCommandHandler(new PlanOfActionsAndMilestonesCommand());
    processor.addCommandHandler(new MetaschemaCommand());
    processor.addCommandHandler(new ValidateCommand());
    processor.addCommandHandler(new ConvertCommand());
    processor.addCommandHandler(new ResolveCommand());
    processor.addCommandHandler(new ListAllowedValuesCommand());
    return processor.process(args);
  }

  private CLI() {
    // disable construction
  }
}
