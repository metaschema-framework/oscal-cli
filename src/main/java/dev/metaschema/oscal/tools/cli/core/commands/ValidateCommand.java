/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands;

import dev.metaschema.cli.processor.CallingContext;
import dev.metaschema.cli.processor.command.ICommandExecutor;
import dev.metaschema.core.model.util.JsonUtil;
import dev.metaschema.core.model.util.XmlUtil;
import dev.metaschema.core.model.validation.JsonSchemaContentValidator;
import dev.metaschema.core.model.validation.XmlSchemaContentValidator;
import dev.metaschema.core.util.CollectionUtil;
import dev.metaschema.core.util.ObjectUtils;
import dev.metaschema.oscal.lib.OscalBindingContext;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A CLI command that supports validating that an OSCAL instance is valid.
 */
public class ValidateCommand
    extends AbstractOscalValidationCommand {
  @Override
  public String getDescription() {
    return "Check that the specified OSCAL instance is well-formed and valid to an OSCAL model.";
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new AbstractOscalValidationCommand.OscalValidationCommandExecutor(callingContext, commandLine);
  }

  @Override
  @NonNull
  public XmlSchemaContentValidator getOscalXmlSchemas() throws IOException {
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
