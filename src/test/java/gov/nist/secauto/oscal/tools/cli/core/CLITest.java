/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

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
                null));
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
                null));

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
                null));
        values.add(
            Arguments.of(
                new String[] {
                    "validate",
                    "-o",
                    "target/" + cmd + "-valid-" + format.name().toLowerCase(Locale.ROOT) + "-sarif.json",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension).toString()
                },
                ExitCode.OK,
                null));

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
                  null));
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
                  null));

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
                  null));
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
                  null));
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
                  null));
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
}
