/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.metaschema.cli.processor.CLIProcessor;
import dev.metaschema.cli.processor.ExitStatus;
import dev.metaschema.cli.processor.command.CommandService;
import dev.metaschema.core.MetaschemaJavaVersion;
import dev.metaschema.core.model.MetaschemaVersion;
import dev.metaschema.core.util.CollectionUtil;
import dev.metaschema.core.util.IVersionInfo;
import dev.metaschema.oscal.lib.LibOscalVersion;
import dev.metaschema.oscal.lib.OscalVersion;
import dev.metaschema.oscal.tools.cli.core.commands.ConvertCommand;
import dev.metaschema.oscal.tools.cli.core.commands.ListAllowedValuesCommand;
import dev.metaschema.oscal.tools.cli.core.commands.ResolveCommand;
import dev.metaschema.oscal.tools.cli.core.commands.ValidateCommand;
import dev.metaschema.oscal.tools.cli.core.commands.assessmentplan.AssessmentPlanCommand;
import dev.metaschema.oscal.tools.cli.core.commands.assessmentresults.AssessmentResultsCommand;
import dev.metaschema.oscal.tools.cli.core.commands.catalog.CatalogCommand;
import dev.metaschema.oscal.tools.cli.core.commands.componentdefinition.ComponentDefinitionCommand;
import dev.metaschema.oscal.tools.cli.core.commands.metaschema.MetaschemaCommand;
import dev.metaschema.oscal.tools.cli.core.commands.poam.PlanOfActionsAndMilestonesCommand;
import dev.metaschema.oscal.tools.cli.core.commands.profile.ProfileCommand;
import dev.metaschema.oscal.tools.cli.core.commands.ssp.SystemSecurityPlanCommand;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides the main entry point for executing the command line interface.
 */
@SuppressWarnings("PMD.ShortClassName")
public final class CLI {
  /**
   * Executes the CLI and handled the exit code.
   *
   * @param args
   *          the CLI arguments
   */
  public static void main(String[] args) {
    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

    ExitStatus status = runCli(args);
    int exitCode = status.getExitCode().getStatusCode();
    System.exit(exitCode);
  }

  /**
   * Executes the CLI.
   *
   * @param args
   *          the CLI arguments
   * @return the result of executing the CLI
   */
  @NonNull
  public static ExitStatus runCli(String... args) {
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    Map<String, IVersionInfo> versions = new LinkedHashMap<>();
    versions.put(CLIProcessor.COMMAND_VERSION, new OscalCliVersion());
    versions.put("https://github.com/usnistgov/liboscal-java", new LibOscalVersion());
    versions.put("https://github.com/usnistgov/OSCAL", new OscalVersion());
    versions.put("https://github.com/usnistgov/metaschema-java", new MetaschemaJavaVersion());
    versions.put("https://github.com/usnistgov/metaschema", new MetaschemaVersion());

    CLIProcessor processor = new CLIProcessor("oscal-cli", CollectionUtil.unmodifiableMap(versions));
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

    // Register SPI-discovered commands (e.g., shell-completion)
    CommandService.getInstance().getCommands().forEach(processor::addCommandHandler);

    return processor.process(args);
  }

  private CLI() {
    // disable construction
  }
}
