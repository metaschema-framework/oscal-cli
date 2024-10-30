/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.model.util.XmlUtil;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ValidateCommand
    extends AbstractOscalValidationCommand {
  @Override
  public String getDescription() {
    return "Check that the specified OSCAL instance is well-formed and valid to an OSCAL model.";
  }

  @Override
  @NonNull
  public XmlSchemaContentValidator getOscalXmlSchemas() throws IOException, SAXException {
    List<Source> retval = new LinkedList<>();
    retval.add(
        XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
            OscalBindingContext.class.getResource("/schema/xml/oscal-complete_schema.xsd"))));
    return new XmlSchemaContentValidator(CollectionUtil.unmodifiableList(retval));
  }

  @Override
  @NonNull
  public JsonSchemaContentValidator getOscalJsonSchema() throws IOException {
    try (InputStream is = ObjectUtils.requireNonNull(
        OscalBindingContext.class.getResourceAsStream("/schema/json/oscal-complete_schema.json"))) {
      return new JsonSchemaContentValidator(JsonUtil.toJsonObject(is));
    }
  }
}
