/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.commands.AbstractValidateContentCommand;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.ExternalConstraintsModulePostProcessor;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.oscal.lib.OscalBindingContext;

import org.apache.commons.cli.CommandLine;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractOscalValidationCommand
    extends AbstractValidateContentCommand {

  @NonNull
  protected abstract List<Source> getOscalXmlSchemas() throws IOException;

  @NonNull
  protected abstract JSONObject getOscalJsonSchema() throws IOException;

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new OscalCommandExecutor(callingContext, commandLine);
  }

  protected class OscalCommandExecutor
      extends AbstractValidationCommandExecutor {

    protected OscalCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    protected IBindingContext getBindingContext(@NonNull Set<IConstraintSet> constraintSets) {
      IBindingContext retval;
      if (constraintSets.isEmpty()) {
        retval = OscalBindingContext.instance();
      } else {
        ExternalConstraintsModulePostProcessor postProcessor
            = new ExternalConstraintsModulePostProcessor(constraintSets);

        retval = new OscalBindingContext(CollectionUtil.singletonList(postProcessor));
      }
      return retval;
    }

    @Override
    @NonNull
    public List<Source> getXmlSchemas(URL targetResource) throws IOException {
      return getOscalXmlSchemas();
    }

    @Override
    @NonNull
    public JSONObject getJsonSchema(JSONObject json) throws IOException {
      return getOscalJsonSchema();
    }
  }
}
