/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.operations;

import gov.nist.secauto.metaschema.core.model.util.XmlUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import net.sf.saxon.jaxp.SaxonTransformerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public final class XMLOperations {
  private static final Logger LOGGER = LogManager.getLogger(XMLOperations.class);

  private XMLOperations() {
    // disable construction
  }

  /**
   * Render the input catalog as HTML.
   *
   * @param input
   *          the source to render
   * @param result
   *          the file to write the rendered HTML to
   * @throws IOException
   *           if an error occurred while loading the input
   * @throws TransformerException
   *           if an error occurred while creating the rendered content
   */
  public static void renderCatalogHTML(File input, File result) throws IOException, TransformerException {
    render(input, result,
        XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
            XMLOperations.class.getResource("/xsl/oscal-for-bootstrap-html.xsl"))));
  }

  /**
   * Render the input profile as HTML.
   *
   * @param input
   *          the source to render
   * @param result
   *          the file to write the rendered HTML to
   * @throws IOException
   *           if an error occurred while loading the input
   * @throws TransformerException
   *           if an error occurred while creating the rendered content
   */
  public static void renderProfileHTML(File input, File result) throws IOException, TransformerException {
    SaxonTransformerFactory transfomerFactory = (SaxonTransformerFactory) TransformerFactory.newInstance();
    // Templates resolver = transfomerFactory.newTemplates();
    // Templates renderer = transfomerFactory.newTemplates();

    File temp = File.createTempFile("resolved-profile", ".xml");

    try {
      Transformer transformer = transfomerFactory.newTransformer(
          XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
              XMLOperations.class.getResource("/xsl/profile-resolver.xsl"))));
      transformer.transform(new StreamSource(input), new StreamResult(temp));

      transformer = transfomerFactory.newTransformer(
          XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
              XMLOperations.class.getResource("/xsl/oscal-for-bootstrap-html.xsl"))));
      transformer.transform(new StreamSource(temp), new StreamResult(result));
    } finally {
      if (!temp.delete()) {
        LOGGER.atError().log("failed to delete file: {}", temp);
      }
    }

    // TransformerHandler resolverHandler =
    // transfomerFactory.newTransformerHandler(resolver);
    // TransformerHandler rendererHandler =
    // transfomerFactory.newTransformerHandler(renderer);
    //
    // resolverHandler.setResult(new SAXResult(rendererHandler));
    // rendererHandler.setResult(new StreamResult(result));
    //
    // Transformer t = transfomerFactory.newTransformer();
    // File sourceFile = input.getAbsoluteFile();
    // StreamSource source = new StreamSource();
    // String sourceSystemId = sourceFile.toURI().toASCIIString();
    // log.info("Source: "+sourceSystemId);
    // source.setSystemId(sourceSystemId);
    // t.setURIResolver(new LoggingURIResolver(t.getURIResolver()));
    // resolver.setParameter("document-uri", sourceSystemId);
    // t.transform(source, new SAXResult(resolverHandler));
  }

  private static void render(File input, File result, Source transform) throws TransformerException {
    TransformerFactory transfomerFactory = TransformerFactory.newInstance();
    assert transfomerFactory instanceof SaxonTransformerFactory;
    Transformer transformer = transfomerFactory.newTransformer(transform);
    transformer.transform(new StreamSource(input), new StreamResult(result));
  }
}
