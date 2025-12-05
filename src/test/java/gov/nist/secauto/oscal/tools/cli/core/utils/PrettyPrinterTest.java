/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.utils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for the {@link PrettyPrinter} utility class.
 * <p>
 * This feature was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
 * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
 * </p>
 */
class PrettyPrinterTest {

  @TempDir
  Path tempDir;

  @Test
  void testPrettyPrintJsonValidFile() throws IOException {
    // Create a minified JSON file
    File jsonFile = tempDir.resolve("test.json").toFile();
    String minifiedJson = "{\"name\":\"test\",\"value\":123,\"nested\":{\"key\":\"value\"}}";
    Files.writeString(jsonFile.toPath(), minifiedJson, StandardCharsets.UTF_8);

    // Pretty print it
    assertDoesNotThrow(() -> PrettyPrinter.prettyPrintJson(jsonFile));

    // Verify the output has proper formatting
    String content = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(content.contains("\n"), "Pretty-printed JSON should have line breaks"),
        () -> assertTrue(content.contains("  ") || content.contains("\t"),
            "Pretty-printed JSON should have indentation"));
  }

  @Test
  void testPrettyPrintJsonInvalidFile() throws IOException {
    // Create an invalid JSON file
    File jsonFile = tempDir.resolve("invalid.json").toFile();
    Files.writeString(jsonFile.toPath(), "{ invalid json }", StandardCharsets.UTF_8);

    // Should throw an exception
    assertThrows(Exception.class, () -> PrettyPrinter.prettyPrintJson(jsonFile));
  }

  @Test
  void testPrettyPrintYamlValidFile() throws IOException {
    // Create a YAML file
    File yamlFile = tempDir.resolve("test.yaml").toFile();
    String yaml = "name: test\nvalue: 123\nnested:\n  key: value";
    Files.writeString(yamlFile.toPath(), yaml, StandardCharsets.UTF_8);

    // Pretty print it
    assertDoesNotThrow(() -> PrettyPrinter.prettyPrintYaml(yamlFile));

    // Verify the file still exists and has content
    String content = Files.readString(yamlFile.toPath(), StandardCharsets.UTF_8);
    assertTrue(!content.isBlank(), "Pretty-printed YAML should have content");
  }

  @Test
  void testPrettyPrintXmlValidFile() throws IOException {
    // Create a minified XML file
    File xmlFile = tempDir.resolve("test.xml").toFile();
    String minifiedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child attr=\"value\">text</child></root>";
    Files.writeString(xmlFile.toPath(), minifiedXml, StandardCharsets.UTF_8);

    // Pretty print it
    assertDoesNotThrow(() -> PrettyPrinter.prettyPrintXml(xmlFile));

    // Verify the output has proper formatting
    String content = Files.readString(xmlFile.toPath(), StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(content.contains("\n"), "Pretty-printed XML should have line breaks"),
        () -> assertTrue(content.contains("<root>"), "Pretty-printed XML should preserve elements"));
  }

  @Test
  void testPrettyPrintXmlInvalidFile() throws IOException {
    // Create an invalid XML file
    File xmlFile = tempDir.resolve("invalid.xml").toFile();
    Files.writeString(xmlFile.toPath(), "<invalid xml", StandardCharsets.UTF_8);

    // Should throw an exception
    assertThrows(Exception.class, () -> PrettyPrinter.prettyPrintXml(xmlFile));
  }

  @Test
  void testPrettyPrintJsonEmptyObject() throws IOException {
    // Create an empty JSON object file
    File jsonFile = tempDir.resolve("empty.json").toFile();
    Files.writeString(jsonFile.toPath(), "{}", StandardCharsets.UTF_8);

    // Pretty print should work without throwing
    assertDoesNotThrow(() -> PrettyPrinter.prettyPrintJson(jsonFile));

    String content = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
    assertTrue(content.contains("{"), "Pretty-printed JSON should still be valid");
  }

  @Test
  void testPrettyPrintJsonArray() throws IOException {
    // Create a JSON array file
    File jsonFile = tempDir.resolve("array.json").toFile();
    String jsonArray = "[{\"id\":1},{\"id\":2},{\"id\":3}]";
    Files.writeString(jsonFile.toPath(), jsonArray, StandardCharsets.UTF_8);

    // Pretty print it
    assertDoesNotThrow(() -> PrettyPrinter.prettyPrintJson(jsonFile));

    // Verify the output has proper formatting
    String content = Files.readString(jsonFile.toPath(), StandardCharsets.UTF_8);
    assertTrue(content.contains("\n"), "Pretty-printed JSON array should have line breaks");
  }
}
