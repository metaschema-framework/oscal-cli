/*
 * SPDX-FileCopyrightText: none
 * SPDX-License-Identifier: CC0-1.0
 */

package gov.nist.secauto.oscal.tools.cli.core.commands;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ResolveCommand
    extends AbstractResolveCommand {

  @NonNull
  private static final String COMMAND = "resolve-profile";

  @Override
  public String getName() {
    return COMMAND;
  }
}
