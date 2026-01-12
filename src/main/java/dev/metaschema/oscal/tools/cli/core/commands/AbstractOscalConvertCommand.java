/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import dev.metaschema.cli.commands.AbstractConvertSubcommand;
import dev.metaschema.cli.processor.CallingContext;
import dev.metaschema.cli.processor.command.CommandExecutionException;
import dev.metaschema.cli.processor.command.ICommandExecutor;
import dev.metaschema.core.model.IBoundObject;
import dev.metaschema.databind.IBindingContext;
import dev.metaschema.databind.io.Format;
import dev.metaschema.databind.io.IBoundLoader;
import dev.metaschema.oscal.lib.OscalBindingContext;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Used by implementing classes to provide an OSCAL content conversion command.
 * <p>
 * This executor provides user feedback about extending command being deprecated
 * in favor of the {@link ConvertCommand}.
 */
public abstract class AbstractOscalConvertCommand
    extends AbstractConvertSubcommand {
  private static final Logger LOGGER = LogManager.getLogger(AbstractOscalConvertCommand.class);

  /**
   * Get the bound object class for the assembly associated with the command.
   *
   * @return the bound object class for the associated assembly
   */
  @NonNull
  public abstract Class<? extends IBoundObject> getOscalClass();

  @Override
  public ICommandExecutor newExecutor(CallingContext callingContext, CommandLine commandLine) {
    return new CommandExecutor(callingContext, commandLine);
  }

  private final class CommandExecutor
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
    public void execute() throws CommandExecutionException {
      LOGGER.atWarn().log("This command path is deprecated. Please use 'convert'.");

      super.execute();
    }

    @Override
    protected void handleConversion(URI source, Format toFormat, Writer writer, IBoundLoader loader)
        throws FileNotFoundException, IOException {
      Class<? extends IBoundObject> clazz = getOscalClass();
      loader.convert(source, writer, toFormat, clazz);
    }
  }
}
