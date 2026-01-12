/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Utility class for pretty-printing output files in various formats.
 * <p>
 * This class was originally contributed by Mahesh Kumar Gaddam (ermahesh) in
 * <a href="https://github.com/usnistgov/oscal-cli/pull/295">PR #295</a>.
 * </p>
 */
public final class PrettyPrinter {

  private PrettyPrinter() {
    // prevent instantiation
  }

  /**
   * Pretty-prints a JSON file in place.
   *
   * @param file
   *          the JSON file to pretty-print
   * @throws IOException
   *           if an I/O error occurs
   */
  public static void prettyPrintJson(@NonNull File file) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Object obj = mapper.readValue(file, Object.class);
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
  }

  /**
   * Pretty-prints a YAML file in place.
   *
   * @param file
   *          the YAML file to pretty-print
   * @throws IOException
   *           if an I/O error occurs
   */
  public static void prettyPrintYaml(@NonNull File file) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    Object obj = mapper.readValue(file, Object.class);
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
  }

  /**
   * Pretty-prints an XML file in place.
   *
   * @param file
   *          the XML file to pretty-print
   * @throws IOException
   *           if an I/O error occurs
   * @throws ParserConfigurationException
   *           if a DocumentBuilder cannot be created
   * @throws SAXException
   *           if any parse errors occur
   * @throws TransformerException
   *           if an error occurs during transformation
   */
  public static void prettyPrintXml(@NonNull File file)
      throws IOException, ParserConfigurationException, SAXException, TransformerException {
    Document doc = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder().parse(file);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    try (OutputStream out = Files.newOutputStream(file.toPath())) {
      transformer.transform(new DOMSource(doc), new StreamResult(out));
    }
  }
}
