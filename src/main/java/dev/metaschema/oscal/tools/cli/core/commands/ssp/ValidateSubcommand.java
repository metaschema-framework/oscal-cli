/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands.ssp;

import dev.metaschema.core.model.util.JsonUtil;
import dev.metaschema.core.model.util.XmlUtil;
import dev.metaschema.core.model.validation.JsonSchemaContentValidator;
import dev.metaschema.core.model.validation.XmlSchemaContentValidator;
import dev.metaschema.core.util.CollectionUtil;
import dev.metaschema.core.util.ObjectUtils;
import dev.metaschema.oscal.lib.OscalBindingContext;
import dev.metaschema.oscal.tools.cli.core.commands.AbstractDeprecatedOscalValidationSubcommand;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;

class ValidateSubcommand
    extends AbstractDeprecatedOscalValidationSubcommand {
  @Override
  public String getDescription() {
    return "Check that the specified OSCAL instance is well-formed and valid to the System Security Plan model.";
  }

  @Override
  protected XmlSchemaContentValidator getOscalXmlSchemas() throws IOException {
    List<Source> retval = new LinkedList<>();
    retval.add(
        XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
            OscalBindingContext.class.getResource("/schema/xml/oscal-ssp_schema.xsd"))));
    return new XmlSchemaContentValidator(CollectionUtil.unmodifiableList(retval));
  }

  @Override
  protected JsonSchemaContentValidator getOscalJsonSchema() throws IOException {
    try (InputStream is = ObjectUtils.requireNonNull(
        OscalBindingContext.class.getResourceAsStream("/schema/json/oscal-ssp_schema.json"))) {
      return new JsonSchemaContentValidator(JsonUtil.toJsonObject(is));
    }
  }
}
