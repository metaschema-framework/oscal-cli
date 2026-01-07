OSCAL Command line Tool v${project.version}

Overview:
---------

This open-source, tool offers a convenient way to manipulate OSCAL and Metaschema based
content supporting the following operations:

- Converting OSCAL content between the OSCAL XML, JSON, and YAML formats.
- Validating an OSCAL resources to ensure it is well-formed and valid.
- Resolving OSCAL Profiles.
- Validating a Metaschema model definition to ensure it is well-formed and valid.
- Generating XML and JSON Schemas from a Metaschema model definition.

More information can be found at: https://github.com/metaschema-framework/oscal-cli

Requirements:
-------------

Requires installation of a Java runtime environment version 11 or newer

Use:
----

The tool has an integrated help feature that explains the various command line options and commands.

The tool can be run as follows:

oscal-cli --help

Disabling Color Output:
-----------------------

The CLI uses ANSI escape codes for colored output, which is supported by most
modern terminals including Windows 10+, Linux, and macOS. If you are using a
legacy console that does not support ANSI escape codes (e.g., older Windows
cmd.exe, certain CI/CD environments, or when redirecting output to a file),
you may see raw escape sequences in the output.

To disable colored output, use the --no-color flag:

  oscal-cli --no-color <command>

Feedback:
---------

Please post issues about tool defects, enhancement requests, and any other related
comments in the tool's GitHub repository at https://github.com/metaschema-framework/oscal-cli.

Change Log:
----------

For change logs, please goto https://github.com/metaschema-framework/oscal-cli/releases.
