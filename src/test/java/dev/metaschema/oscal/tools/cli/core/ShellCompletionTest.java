/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.metaschema.cli.processor.ExitCode;
import dev.metaschema.cli.processor.ExitStatus;

/**
 * Tests for the shell completion command.
 * <p>
 * This addresses issue #85: Shell Completions.
 * </p>
 */
class ShellCompletionTest {

  /**
   * Tests that the shell-completion command is available and returns help.
   */
  @Test
  void testShellCompletionHelp() {
    String[] args = { "shell-completion", "-h" };
    ExitStatus status = CLI.runCli(args);

    assertEquals(ExitCode.OK, status.getExitCode(),
        "shell-completion -h should return OK");
  }

  /**
   * Tests that shell-completion generates bash completion script.
   */
  @Test
  void testShellCompletionBash() {
    String[] args = { "shell-completion", "bash" };
    ExitStatus status = CLI.runCli(args);

    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode(),
            "shell-completion bash should return OK"),
        () -> assertNull(status.getThrowable(),
            "shell-completion bash should not throw"));
  }

  /**
   * Tests that shell-completion generates zsh completion script.
   */
  @Test
  void testShellCompletionZsh() {
    String[] args = { "shell-completion", "zsh" };
    ExitStatus status = CLI.runCli(args);

    assertAll(
        () -> assertEquals(ExitCode.OK, status.getExitCode(),
            "shell-completion zsh should return OK"),
        () -> assertNull(status.getThrowable(),
            "shell-completion zsh should not throw"));
  }

  /**
   * Tests that shell-completion can write to a file.
   */
  @ParameterizedTest
  @ValueSource(strings = { "bash", "zsh" })
  void testShellCompletionToFile(String shell) throws IOException {
    Path outputPath = Path.of("target/completion." + shell);
    String[] args = { "shell-completion", shell, "--to", outputPath.toString() };

    try {
      ExitStatus status = CLI.runCli(args);

      assertAll(
          () -> assertEquals(ExitCode.OK, status.getExitCode(),
              "shell-completion " + shell + " --to file should return OK"),
          () -> assertTrue(Files.exists(outputPath),
              "Output file should exist"),
          () -> {
            String content = Files.readString(outputPath, StandardCharsets.UTF_8);
            assertTrue(content.contains("oscal-cli"),
                "Completion script should reference oscal-cli");
          });
    } finally {
      // Clean up - always runs even if assertions fail
      Files.deleteIfExists(outputPath);
    }
  }

  /**
   * Tests that shell-completion with invalid shell type fails gracefully.
   */
  @Test
  void testShellCompletionInvalidShell() {
    String[] args = { "shell-completion", "fish" };
    ExitStatus status = CLI.runCli(args);

    // Should fail with an invalid argument error
    assertEquals(ExitCode.INVALID_ARGUMENTS, status.getExitCode(),
        "shell-completion with unsupported shell should fail");
  }

  /**
   * Tests that shell-completion without arguments shows help.
   */
  @Test
  void testShellCompletionNoArgs() {
    String[] args = { "shell-completion" };
    ExitStatus status = CLI.runCli(args);

    // Without the required shell argument, should show usage/help
    assertEquals(ExitCode.INVALID_ARGUMENTS, status.getExitCode(),
        "shell-completion without shell argument should fail");
  }
}
