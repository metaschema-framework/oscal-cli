/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.metaschema.cli.commands.AbstractConvertSubcommand;
import gov.nist.secauto.metaschema.cli.processor.CLIProcessor.CallingContext;
import gov.nist.secauto.metaschema.cli.processor.command.ICommandExecutor;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.FormatDetector;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.io.ModelDetector;
import gov.nist.secauto.oscal.lib.OscalBindingContext;

import org.apache.commons.cli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides an OSCAL content conversion command.
 */
public class ConvertCommand
    extends AbstractConvertSubcommand {
  @Override
  public String getDescription() {
    return "Check that the specified OSCAL instance is well-formed and valid to an OSCAL model.";
  }

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new CommandExecutor(callingContext, commandLine);
  }

  private static final class CommandExecutor
      extends AbstractConversionCommandExecutor {

    private CommandExecutor(
        @NonNull CallingContext callingContext,
        @NonNull CommandLine commandLine) {
      super(callingContext, commandLine);
    }

    @Override
    protected IBindingContext getBindingContext() {
      return OscalBindingContext.instance();
    }

    @Override
    protected void handleConversion(URI source, Format toFormat, Writer writer, IBoundLoader loader)
        throws FileNotFoundException, IOException {

      Class<? extends IBoundObject> boundClass;
      IBoundObject object;
      try (InputStream is = source.toURL().openStream()) {
        assert is != null;
        FormatDetector.Result formatResult = loader.detectFormat(is, source);
        Format sourceformat = formatResult.getFormat();
        try (InputStream fis = formatResult.getDataStream()) {
          try (ModelDetector.Result modelResult = loader.detectModel(fis, source, sourceformat)) {
            boundClass = modelResult.getBoundClass();
            try (InputStream mis = modelResult.getDataStream()) {
              object = loader.load(boundClass, sourceformat, mis, source);
            }
          }
        }
      }
      ISerializer<?> serializer = getBindingContext().newSerializer(toFormat, boundClass);
      serializer.serialize(object, writer);
    }
  }
}
