/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package dev.metaschema.oscal.tools.cli.core.commands;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A CLI command that supports resolving an OSCAL profile into a resolved
 * catalog.
 */
public class ResolveCommand
    extends AbstractResolveCommand {

  @NonNull
  private static final String COMMAND = "resolve-profile";

  @Override
  public String getName() {
    return COMMAND;
  }
}
