/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.metaschema.core.model.util.JsonUtil;
import gov.nist.secauto.metaschema.core.model.util.XmlUtil;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.tools.cli.core.commands.oscal.AbstractDeprecatedOscalValidationSubcommand;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;

public class ValidateSubcommand
    extends AbstractDeprecatedOscalValidationSubcommand {
  @Override
  public String getDescription() {
    return "Check that the specified OSCAL Profile is well-formed and valid to the Profile model.";
  }

  @Override
  protected List<Source> getOscalXmlSchemas() throws IOException {
    List<Source> retval = new LinkedList<>();
    retval.add(
        XmlUtil.getStreamSource(ObjectUtils.requireNonNull(
            OscalBindingContext.class.getResource("/schema/xml/oscal-profile_schema.xsd"))));
    return CollectionUtil.unmodifiableList(retval);
  }

  @Override
  protected JSONObject getOscalJsonSchema() throws IOException {
    try (InputStream is = ObjectUtils.requireNonNull(
        OscalBindingContext.class.getResourceAsStream("/schema/json/oscal-profile_schema.json"))) {
      return JsonUtil.toJsonObject(is);
    }
  }
}
