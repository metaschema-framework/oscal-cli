/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.commands.AbstractConvertSubcommand;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.oscal.lib.OscalBindingContext;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractOscalConvertSubcommand
    extends AbstractConvertSubcommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractOscalConvertSubcommand.class);

  @NonNull
  public abstract Class<? extends IBoundObject> getOscalClass();

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new OscalCommandExecutor(callingContext, commandLine);
  }

  private final class OscalCommandExecutor
      extends AbstractConversionCommandExecutor {

    private OscalCommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    protected IBindingContext getBindingContext() {
      return OscalBindingContext.instance();
    }

    @Override
    public ExitStatus execute() {
      LOGGER.atWarn().log("This command path is deprecated. Please use 'convert'.");

      return super.execute();
    }

    @Override
    protected void handleConversion(URI source, Format toFormat, Writer writer, IBoundLoader loader)
        throws FileNotFoundException, IOException {
      Class<? extends IBoundObject> clazz = getOscalClass();
      loader.convert(source, writer, toFormat, clazz);
    }
  }
}
