/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import dev.metaschema.cli.processor.ExitCode;
import dev.metaschema.cli.processor.ExitStatus;
import dev.metaschema.core.util.ObjectUtils;
import dev.metaschema.databind.io.Format;
import dev.metaschema.oscal.lib.profile.resolver.ProfileResolutionException;
import edu.umd.cs.findbugs.annotations.NonNull;

class CLITest {
  private static final Throwable NO_THROWABLE_RESULT = null;

  private static String generateOutputPath(
      @NonNull Path source,
      @NonNull String extra,
      @NonNull Format targetFormat)
      throws IOException {
    String filename = ObjectUtils.notNull(source.getFileName()).toString();

    int pos = filename.lastIndexOf('.');
    filename = filename.substring(0, pos) + "_" + extra + "_converted" + targetFormat.getDefaultExtension();

    Path dir = Files.createDirectories(Path.of("target/oscal-cli-convert"));

    return dir.resolve(filename).toString();
  }

  private static Stream<Arguments> providesValues() throws IOException {
    final String[] commands = { "ap", "ar", "catalog", "component-definition", "profile", "poam", "ssp" };
    final Map<Format, List<Format>> formatEntries = Map.of(
        Format.XML, Arrays.asList(Format.JSON, Format.YAML),
        Format.JSON, Arrays.asList(Format.XML, Format.JSON),
        Format.YAML, Arrays.asList(Format.XML, Format.JSON));
    List<Arguments> values = new ArrayList<>();

    values.add(Arguments.of(new String[] { "--version" }, ExitCode.OK, null));
    for (String cmd : commands) {
      // test helps
      values.add(Arguments.of(new String[] { cmd, "validate", "-h" }, ExitCode.OK, null));
      values.add(Arguments.of(new String[] { cmd, "convert", "-h" }, ExitCode.OK, null));

      for (Format format : Format.values()) {
        String sourceExtension = format.getDefaultExtension();
        // test command path-specific commands
        values.add(
            Arguments.of(
                new String[] {
                    cmd,
                    "validate",
                    "-o",
                    "target/" + cmd + "-invalid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension).toString()
                },
                ExitCode.FAIL,
                NO_THROWABLE_RESULT));
        values.add(
            Arguments.of(
                new String[] {
                    cmd,
                    "validate",
                    "-o",
                    "target/" + cmd + "-valid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension).toString()
                },
                ExitCode.OK,
                NO_THROWABLE_RESULT));

        // test general commands
        values.add(
            Arguments.of(
                new String[] {
                    "validate",
                    "-o",
                    "target/general-" + cmd + "-invalid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension).toString()
                },
                ExitCode.FAIL,
                NO_THROWABLE_RESULT));
        values.add(
            Arguments.of(
                new String[] {
                    "validate",
                    "-o",
                    "target/general-" + cmd + "-valid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension).toString()
                },
                ExitCode.OK,
                NO_THROWABLE_RESULT));

        for (Format targetFormat : formatEntries.get(format)) {
          Path path = Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension);
          // test command path-specific command
          values.add(
              Arguments.of(
                  new String[] {
                      cmd,
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(Locale.ROOT),
                      path.toString(),
                      generateOutputPath(path, "convert-" + cmd + "-" + format.name(), targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  NO_THROWABLE_RESULT));
          // test general command
          values.add(
              Arguments.of(
                  new String[] {
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(Locale.ROOT),
                      path.toString(),
                      generateOutputPath(path, "convert-general-" + format.name(), targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  NO_THROWABLE_RESULT));

          // test command path-specific command
          path = Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension);
          values.add(
              Arguments.of(
                  new String[] {
                      cmd,
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(Locale.ROOT),
                      path.toString(),
                      generateOutputPath(path, "convert-" + cmd + "-" + format.name(), targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  NO_THROWABLE_RESULT));
          // test general command
          values.add(
              Arguments.of(
                  new String[] {
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(Locale.ROOT),
                      path.toString(),
                      generateOutputPath(path, "convert-general-" + format.name(), targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  NO_THROWABLE_RESULT));
        }
        if ("profile".equals(cmd)) {
          // test command path-specific command
          values.add(
              Arguments.of(
                  new String[] {
                      "profile",
                      "resolve",
                      "--to=" + format.name().toLowerCase(Locale.ROOT),
                      Paths.get("src/test/resources/cli/example_profile_valid" + sourceExtension).toString()
                  },
                  ExitCode.OK,
                  NO_THROWABLE_RESULT));
          values.add(
              Arguments.of(
                  new String[] {
                      "profile",
                      "resolve",
                      "--to=" + format.name().toLowerCase(Locale.ROOT),
                      Paths.get("src/test/resources/cli/example_profile_invalid" + sourceExtension).toString()
                  },
                  ExitCode.PROCESSING_ERROR,
                  ProfileResolutionException.class));
          // test general command
          values.add(
              Arguments.of(
                  new String[] {
                      "resolve-profile",
                      "--to=" + format.name().toLowerCase(Locale.ROOT),
                      Paths.get("src/test/resources/cli/example_profile_valid" + sourceExtension).toString()
                  },
                  ExitCode.OK,
                  null));
          values.add(
              Arguments.of(
                  new String[] {
                      "resolve-profile",
                      "--to=" + format.name().toLowerCase(Locale.ROOT),
                      Paths.get("src/test/resources/cli/example_profile_invalid" + sourceExtension).toString()
                  },
                  ExitCode.PROCESSING_ERROR,
                  ProfileResolutionException.class));
        }
      }
    }

    values.add(Arguments.of(new String[] { "metaschema", "metapath", "list-functions" }, ExitCode.OK, null));

    return values.stream();
  }

  @ParameterizedTest
  @MethodSource("providesValues")
  void testAllSubCommands(
      @NonNull String[] commandArgs,
      @NonNull ExitCode expectedExitCode,
      Class<? extends Throwable> expectedThrownClass) {
    List<String> execArgs = new LinkedList<>(Arrays.asList(commandArgs));
    execArgs.add("--show-stack-trace");

    String[] args = execArgs.toArray(new String[0]);
    ExitStatus exitStatus = CLI.runCli(args);

    exitStatus.generateMessage(true);
    if (expectedThrownClass == null) {
      assertAll(
          () -> assertEquals(expectedExitCode, exitStatus.getExitCode(), "exit code mismatch"),
          () -> assertNull(expectedThrownClass, "expected null Throwable"),
          () -> {
            Throwable thrown = exitStatus.getThrowable();
            if (!expectedExitCode.equals(exitStatus.getExitCode())
                && thrown != null) {
              throw thrown;
            }
          });
    } else {
      assertAll(
          () -> assertEquals(expectedExitCode, exitStatus.getExitCode(), "exit code mismatch"),
          () -> assertThrows(expectedThrownClass, () -> {
            Throwable thrown = exitStatus.getThrowable();
            if (thrown != null) {
              throw thrown;
            }
          }, "throwable mismatch"),
          () -> {
            Throwable thrown = exitStatus.getThrowable();
            if (thrown != null && !(expectedExitCode.equals(exitStatus.getExitCode())
                && expectedThrownClass.equals(thrown.getClass()))) {
              throw thrown;
            }
          });
    }
  }

  @Test
  void testSystemSecurityPlanQuietlyFailing() {
    String[] args = {
        "convert",
        "--to=yaml",
        "src/test/resources/cli/quietly_failing_ssp.xml",
        "target/oscal-cli-convert/quietly_failing_ssp_converted.json",
        "--show-stack-trace",
        "--overwrite"
    };

    ExitStatus status = CLI.runCli(args);
    Throwable thrown = status.getThrowable();
    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode()),
        () -> assertEquals(NO_THROWABLE_RESULT, thrown),
        () -> assertNotEquals(Files.size(Paths.get("target/oscal-cli-convert/quietly_failing_ssp_converted.json")), 0));
  }

  @Test
  void testSystemSecurityPlanQuietlyFailing2() {
    String[] args = {
        "validate",
        "src/test/resources/AwesomeCloudSSP1extrainvalid.xml",
        "--show-stack-trace"
    };

    ExitStatus status = CLI.runCli(args);
    Throwable thrown = status.getThrowable();
    assertAll(
        () -> assertEquals(ExitCode.IO_ERROR, status.getExitCode()),
        () -> assertEquals(IOException.class, thrown == null ? null : thrown.getClass()));
  }

  @Test
  void testSystemSecurityPlanQuietlyFailing3() {
    String[] args = {
        "convert",
        "--to=json",
        "src/test/resources/AwesomeCloudSSP1extrainvalid.xml",
        "target/oscal-cli-convert/quietly_failing_ssp_converted2.json",
        "--show-stack-trace",
        "--overwrite"
    };

    ExitStatus status = CLI.runCli(args);
    Throwable thrown = status.getThrowable();
    assertAll(
        () -> assertEquals(ExitCode.IO_ERROR, status.getExitCode()),
        () -> assertEquals(IOException.class, thrown == null ? null : thrown.getClass()));
  }

  /**
   * Tests the --pretty-print option for profile resolution with XML output.
   * <p>
   * This feature was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
   * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
   * </p>
   */
  @Test
  void testPrettyPrintProfileResolveXml() throws IOException {
    Path outputPath = Path.of("target/resolved_pretty_print.xml");
    String[] args = {
        "resolve-profile",
        "--to=xml",
        "--pretty-print",
        "src/test/resources/cli/example_profile_valid.xml",
        outputPath.toString(),
        "--overwrite",
        "--show-stack-trace"
    };

    ExitStatus status = CLI.runCli(args);
    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode()),
        () -> assertTrue(Files.exists(outputPath), "Output file should exist"),
        () -> assertPrettyPrintedOutput(outputPath));
  }

  /**
   * Tests the --pretty-print option for profile resolution with JSON output.
   * <p>
   * This feature was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
   * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
   * </p>
   */
  @Test
  void testPrettyPrintProfileResolveJson() throws IOException {
    Path outputPath = Path.of("target/resolved_pretty_print.json");
    String[] args = {
        "resolve-profile",
        "--to=json",
        "--pretty-print",
        "src/test/resources/cli/example_profile_valid.xml",
        outputPath.toString(),
        "--overwrite",
        "--show-stack-trace"
    };

    ExitStatus status = CLI.runCli(args);
    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode()),
        () -> assertTrue(Files.exists(outputPath), "Output file should exist"),
        () -> assertPrettyPrintedOutput(outputPath));
  }

  /**
   * Tests the --pretty-print option for profile resolution with YAML output.
   * <p>
   * This feature was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
   * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
   * </p>
   */
  @Test
  void testPrettyPrintProfileResolveYaml() throws IOException {
    Path outputPath = Path.of("target/resolved_pretty_print.yaml");
    String[] args = {
        "resolve-profile",
        "--to=yaml",
        "--pretty-print",
        "src/test/resources/cli/example_profile_valid.xml",
        outputPath.toString(),
        "--overwrite",
        "--show-stack-trace"
    };

    ExitStatus status = CLI.runCli(args);
    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode()),
        () -> assertTrue(Files.exists(outputPath), "Output file should exist"),
        () -> assertPrettyPrintedOutput(outputPath));
  }

  private void assertPrettyPrintedOutput(Path outputPath) throws IOException {
    String content = Files.readString(outputPath, StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(content != null && !content.isBlank(), "Content should not be empty"),
        () -> {
          // Naive pretty-print assertion: check for line breaks and indentation
          long lineCount = content.lines().count();
          boolean hasIndentedLines = content.lines()
              .anyMatch(line -> line.startsWith("  ") || line.startsWith("    "));

          assertTrue(lineCount > 5, "Expected multiple lines for pretty-printed output");
          assertTrue(hasIndentedLines, "Expected indented lines in pretty-printed output");
        });
  }
}
