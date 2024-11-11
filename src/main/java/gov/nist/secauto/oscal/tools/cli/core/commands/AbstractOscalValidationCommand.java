/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.commands.AbstractValidateContentCommand;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.command.CommandExecutionException;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext.ISchemaValidationProvider;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.OscalCompleteModule;

import org.apache.commons.cli.CommandLine;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used by implementing classes to provide an OSCAL content validation command.
 */
public abstract class AbstractOscalValidationCommand
    extends AbstractValidateContentCommand {

  /**
   * Load the OSCAL XML schemas.
   *
   * @return the XML schema validator instance
   * @throws IOException
   *           if an error occurred while parsing the provided XML schemas
   */
  @NonNull
  protected abstract XmlSchemaContentValidator getOscalXmlSchemas() throws IOException;

  /**
   * Load the OSCAL JSON schemas.
   *
   * @return the XML schema validator instance
   * @throws IOException
   *           if an error occurred while parsing the provided XML schemas
   */
  @NonNull
  protected abstract JsonSchemaContentValidator getOscalJsonSchema() throws IOException;

  @Override
  public abstract ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine);

  /**
   * Provides OSCAL validation command execution support.
   */
  protected class OscalValidationCommandExecutor
      extends AbstractValidateContentCommand.AbstractValidationCommandExecutor
      implements ISchemaValidationProvider {

    /**
     * Construct a new command executor.
     *
     * @param callingContext
     *          the context of the command execution
     * @param commandLine
     *          the parsed command line details
     */
    protected OscalValidationCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    protected IBindingContext getBindingContext(@NonNull Set<IConstraintSet> constraintSets) {
      return OscalBindingContext.builder()
          .constraintSet(constraintSets)
          .build();
    }

    @Override
    @NonNull
    public XmlSchemaContentValidator getXmlSchemas(URL targetResource, IBindingContext bindingContext)
        throws IOException, SAXException {
      return getOscalXmlSchemas();
    }

    @Override
    @NonNull
    public JsonSchemaContentValidator getJsonSchema(JSONObject json, IBindingContext bindingContext)
        throws IOException {
      return getOscalJsonSchema();
    }

    @Override
    @NonNull
    protected ISchemaValidationProvider getSchemaValidationProvider(
        @NonNull IModule module,
        @NonNull CommandLine commandLine,
        @NonNull IBindingContext bindingContext) {
      return this;
    }

    @Override
    protected IModule getModule(CommandLine commandLine, IBindingContext bindingContext)
        throws CommandExecutionException {
      return bindingContext.registerModule(OscalCompleteModule.class);
    }
  }
}
