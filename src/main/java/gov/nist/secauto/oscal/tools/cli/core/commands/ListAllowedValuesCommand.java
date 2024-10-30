/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import gov.nist.secauto.metaschema.cli.commands.MetaschemaCommands;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.InvalidArgumentException;
import gov.nist.secauto.metaschema.cli.processor.OptionUtils;
import gov.nist.secauto.metaschema.cli.processor.command.AbstractTerminalCommand;
import gov.nist.secauto.metaschema.cli.processor.command.DefaultExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ExtraArgument;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModuleNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValue;
import gov.nist.secauto.metaschema.core.model.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.core.util.UriUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.OscalCompleteModule;
import gov.nist.secauto.oscal.lib.model.util.AllowedValueCollectingNodeItemVisitor;
import gov.nist.secauto.oscal.lib.model.util.AllowedValueCollectingNodeItemVisitor.AllowedValuesRecord;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class ListAllowedValuesCommand
    extends AbstractTerminalCommand {
  private static final Logger LOGGER = LogManager.getLogger(ListAllowedValuesCommand.class);

  @NonNull
  private static final String COMMAND = "list-allowed-values";
  @NonNull
  private static final List<ExtraArgument> EXTRA_ARGUMENTS;
  @NonNull
  private static final Option CONSTRAINTS_OPTION = ObjectUtils.notNull(
      Option.builder("c")
          .hasArgs()
          .argName("URL")
          .desc("additional constraint definitions")
          .build());

  static {
    EXTRA_ARGUMENTS = ObjectUtils.notNull(List.of(
        new DefaultExtraArgument("destination-file", false)));
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  public String getDescription() {
    return "Generate a diagram for the provided Metaschema module";
  }

  @SuppressWarnings("null")
  @Override
  public Collection<? extends Option> gatherOptions() {
    return List.of(
        CONSTRAINTS_OPTION,
        MetaschemaCommands.OVERWRITE_OPTION);
  }

  @Override
  public List<ExtraArgument> getExtraArguments() {
    return EXTRA_ARGUMENTS;
  }

  @Override
  public void validateOptions(CallingContext callingContext, CommandLine cmdLine) throws InvalidArgumentException {
    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.size() > 1) {
      throw new InvalidArgumentException("Illegal number of arguments.");
    }
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine cmdLine) {
    return ICommandExecutor.using(callingContext, cmdLine, this::executeCommand);
  }

  /**
   * Execute the list allowed values command.
   *
   * @param callingContext
   *          information about the calling context
   * @param cmdLine
   *          the parsed command line details
   * @return the execution result
   */
  @SuppressWarnings({
      "PMD.OnlyOneReturn", // readability
      "PMD.AvoidCatchingGenericException"
  })
  protected ExitStatus executeCommand(
      @NonNull CallingContext callingContext,
      @NonNull CommandLine cmdLine) {

    List<String> extraArgs = cmdLine.getArgList();

    Path destination = null;
    if (extraArgs.size() > 1) {
      destination = Paths.get(extraArgs.get(1)).toAbsolutePath();
    }

    if (destination != null) {
      if (Files.exists(destination)) {
        if (!cmdLine.hasOption(MetaschemaCommands.OVERWRITE_OPTION)) {
          return ExitCode.INVALID_ARGUMENTS.exitMessage( // NOPMD readability
              String.format("The provided destination '%s' already exists and the '%s' option was not provided.",
                  destination,
                  OptionUtils.toArgument(MetaschemaCommands.OVERWRITE_OPTION)));
        }
        if (!Files.isWritable(destination)) {
          return ExitCode.IO_ERROR.exitMessage( // NOPMD readability
              "The provided destination '" + destination + "' is not writable.");
        }
      } else {
        Path parent = destination.getParent();
        if (parent != null) {
          try {
            Files.createDirectories(parent);
          } catch (IOException ex) {
            return ExitCode.INVALID_TARGET.exit().withThrowable(ex); // NOPMD readability
          }
        }
      }
    }

    URI cwd = ObjectUtils.notNull(Paths.get("").toAbsolutePath().toUri());

    Set<IConstraintSet> constraintSets;
    if (cmdLine.hasOption(CONSTRAINTS_OPTION)) {
      IConstraintLoader constraintLoader = IBindingContext.getConstraintLoader();
      constraintSets = new LinkedHashSet<>();
      String[] args = cmdLine.getOptionValues(CONSTRAINTS_OPTION);
      for (String arg : args) {
        assert arg != null;
        try {
          URI constraintUri = ObjectUtils.requireNonNull(UriUtils.toUri(arg, cwd));
          constraintSets.addAll(constraintLoader.load(constraintUri));
        } catch (IOException | MetaschemaException | MetapathException | URISyntaxException ex) {
          return ExitCode.IO_ERROR.exitMessage("Unable to load constraint set '" + arg + "'.").withThrowable(ex);
        }
      }
    } else {
      constraintSets = CollectionUtil.emptySet();
    }

    IBindingContext bindingContext;
    try {
      bindingContext = OscalBindingContext.builder()
          .constraintSet(constraintSets)
          .build();
    } catch (Exception ex) {
      return ExitCode.PROCESSING_ERROR
          .exitMessage("Unable to get binding context. " + ex.getMessage())
          .withThrowable(ex);
    }

    IBoundModule module = bindingContext.registerModule(OscalCompleteModule.class);

    try {
      if (destination == null) {
        Writer stringWriter = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
          generateAllowedValuesList(module, writer, null);
        }

        // Print the result
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info(stringWriter.toString());
        }
      } else {
        try (Writer writer = Files.newBufferedWriter(
            destination,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
          try (PrintWriter printWriter = new PrintWriter(writer)) {
            generateAllowedValuesList(module, printWriter, null);
          }
        }
      }

      return ExitCode.OK.exit();
    } catch (IOException ex) {
      return ExitCode.IO_ERROR.exit().withThrowable(ex);
    } catch (Exception ex) {
      return ExitCode.PROCESSING_ERROR.exit().withThrowable(ex);
    }
  }

  private static void generateAllowedValuesList(
      @NonNull IModule module,
      @NonNull PrintWriter writer,
      @Nullable String metapath) throws IOException {
    AllowedValueCollectingNodeItemVisitor walker = new AllowedValueCollectingNodeItemVisitor();

    StaticContext staticContext = StaticContext.builder()
        .defaultModelNamespace(module.getXmlNamespace())
        .build();

    IModuleNodeItem moduleNodeItem = INodeItemFactory.instance().newModuleNodeItem(module);

    // if (metapath != null) {
    // MetapathExpression filter = MetapathExpression.compile(metapath,
    // staticContext);
    //
    // DynamicContext dynamicContext = new DynamicContext(staticContext);
    // ISequence<?> sequence = filter.evaluate(moduleNodeItem, dynamicContext);
    // assert sequence != null;
    // }

    DynamicContext dynamicContext = new DynamicContext(staticContext);
    dynamicContext.disablePredicateEvaluation();

    walker.visit(moduleNodeItem, dynamicContext);

    Map<IDefinitionNodeItem<?, ?>,
        List<AllowedValueCollectingNodeItemVisitor.AllowedValuesRecord>> allowedValuesByTarget
            = ObjectUtils.notNull(walker.getAllowedValueLocations().stream()
                .flatMap(location -> location.getAllowedValues().stream())
                .collect(Collectors.groupingBy(AllowedValuesRecord::getTarget,
                    () -> new TreeMap<>(Comparator.comparing(IDefinitionNodeItem::getMetapath)),
                    Collectors.mapping(Function.identity(), Collectors.toUnmodifiableList()))));

    generateYaml(allowedValuesByTarget, writer);
  }

  private static void generateYaml(
      @NonNull Map<IDefinitionNodeItem<?, ?>, List<AllowedValuesRecord>> allowedValuesByTarget,
      @NonNull PrintWriter writer) throws IOException {

    YAMLFactoryBuilder builder = YAMLFactory.builder();
    YAMLFactory factory = ObjectUtils.notNull(builder
        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        .enable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
        .disable(YAMLGenerator.Feature.SPLIT_LINES)
        .build());

    try (YAMLGenerator generator = factory.createGenerator(writer)) {

      generator.writeStartObject(); // toplevel

      writeLocations(allowedValuesByTarget, generator);

      generator.writeEndObject(); // toplevel
    }
  }

  private static void writeLocations(
      @NonNull Map<IDefinitionNodeItem<?, ?>, List<AllowedValuesRecord>> allowedValuesByTarget,
      @NonNull YAMLGenerator generator) throws IOException {
    generator.writeFieldName("locations");
    generator.writeStartObject(); // locations

    for (Map.Entry<IDefinitionNodeItem<?, ?>, List<AllowedValuesRecord>> entry : allowedValuesByTarget.entrySet()) {
      assert entry != null;
      writeLocation(entry, generator);
    }
    generator.writeEndObject(); // locations
  }

  private static void writeLocation(
      @NonNull Map.Entry<IDefinitionNodeItem<?, ?>, List<AllowedValuesRecord>> entry,
      @NonNull YAMLGenerator generator) throws IOException {

    IDefinitionNodeItem<?, ?> target = ObjectUtils.notNull(entry.getKey());

    generator.writeFieldName(metapath(target));

    generator.writeStartObject(); // metapath

    writeLocationConstraints(entry, generator);

    generator.writeEndObject(); // metapath
  }

  private static void writeLocationConstraints(
      @NonNull Entry<IDefinitionNodeItem<?, ?>, List<AllowedValuesRecord>> entry,
      @NonNull YAMLGenerator generator) throws IOException {
    IDefinitionNodeItem<?, ?> target = ObjectUtils.notNull(entry.getKey());

    List<AllowedValuesRecord> allowedValues = entry.getValue();
    if (allowedValues != null) {
      generator.writeFieldName("constraints");

      generator.writeStartArray(); // constraints

      for (AllowedValuesRecord record : allowedValues) {
        assert target.equals(record.getTarget());

        writeAllowedValue(record, generator);
      }

      generator.writeEndArray(); // constraints
    }
  }

  private static void writeAllowedValue(@NonNull AllowedValuesRecord record, @NonNull YAMLGenerator generator)
      throws IOException {

    generator.writeStartObject(); // constraint

    generator.writeStringField("type", "allowed-values");

    IAllowedValuesConstraint constraint = record.getAllowedValues();
    if (constraint.getId() != null) {
      generator.writeStringField("identifier", constraint.getId());
    }
    generator.writeStringField("location", metapath(record.getLocation()));
    generator.writeStringField("target", constraint.getTarget());

    List<String> values = constraint.getAllowedValues().values().stream()
        .map(IAllowedValue::getValue)
        .collect(Collectors.toList());
    generator.writeFieldName("values");
    if (values == null) {
      generator.writeNull();
    } else {
      generator.writeStartArray();
      for (String value : values) {
        generator.writeString(value);
      }
      generator.writeEndArray();
    }

    generator.writeBooleanField("allow-other", constraint.isAllowedOther());

    URI source = constraint.getSource().getSource();
    generator.writeStringField("source", source == null ? "builtin" : source.toString());

    generator.writeEndObject(); // constraint
  }

  private static String metapath(@NonNull IDefinitionNodeItem<?, ?> item) {
    return metapath(item.getMetapath());
  }

  private static String metapath(@NonNull String path) {
    // remove position 1 predicates
    return path.replace("[1]", "");
  }
}
