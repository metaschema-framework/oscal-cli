/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
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

import edu.umd.cs.findbugs.annotations.NonNull;

class CLITest {
  private static final Throwable NO_THROWABLE_RESULT = null;

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode) {
    assertAll(
        () -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertNull(status.getThrowable(), "expected null Throwable"));
  }

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode,
      @NonNull Class<? extends Throwable> thrownClass) {
    Throwable thrown = status.getThrowable();
    assertAll(
        () -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertEquals(
            thrownClass,
            thrown == null ? null : thrown.getClass(),
            "Throwable mismatch"));
  }

  private static String generateOutputPath(@NonNull Path source, @NonNull Format targetFormat) throws IOException {
    String filename = ObjectUtils.notNull(source.getFileName()).toString();

    int pos = filename.lastIndexOf('.');
    filename = filename.substring(0, pos) + "_converted" + targetFormat.getDefaultExtension();

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
                    "target/" + cmd + "-invalid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension).toString()
                },
                ExitCode.FAIL,
                NO_THROWABLE_RESULT));
        values.add(
            Arguments.of(
                new String[] {
                    "validate",
                    "-o",
                    "target/" + cmd + "-valid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
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
                      generateOutputPath(path, targetFormat),
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
                      generateOutputPath(path, targetFormat),
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
                      generateOutputPath(path, targetFormat),
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
                      generateOutputPath(path, targetFormat),
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
  void testAllSubCommands(@NonNull String[] commandArgs, @NonNull ExitCode expectedExitCode,
      Class<? extends Throwable> expectedThrownClass) {
    List<String> execArgs = new LinkedList<>(Arrays.asList(commandArgs));
    execArgs.add("--show-stack-trace");

    String[] args = execArgs.toArray(new String[0]);

    if (expectedThrownClass == null) {
      evaluateResult(CLI.runCli(args), expectedExitCode);
    } else {
      evaluateResult(CLI.runCli(args), expectedExitCode, expectedThrownClass);
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
        () -> assertEquals(IOException.class, thrown == null ? null : thrown.getClass()),
        () -> assertNotEquals(Files.size(Paths.get("target/oscal-cli-convert/quietly_failing_ssp_converted.json")), 0));
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
        () -> assertEquals(IOException.class, thrown == null ? null : thrown.getClass()),
        () -> assertNotEquals(Files.size(Paths.get("target/oscal-cli-convert/quietly_failing_ssp_converted.json")), 0));
  }
}
