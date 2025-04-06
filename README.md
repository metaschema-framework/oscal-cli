# OSCAL Java Command Line Tool

A Java tool, providing a command line interface, that performs common operations on [Open Security Controls Assessment Language](https://pages.nist.gov/OSCAL/) (OSCAL) and [Metaschema](https://github.com/metaschema-framework/metaschema) content.

This open-source, tool offers a convenient way to manipulate OSCAL and Metaschema based content supporting the following operations:

- Converting OSCAL content between the OSCAL XML, JSON, and YAML formats.
- Validating an OSCAL resources to ensure it is well-formed and valid.
- Resolving OSCAL Profiles.
- Validating a Metaschema model definition to ensure it is well-formed and valid.
- Generating XML and JSON Schemas from a Metaschema model definition.

This work is intended to make it easier for OSCAL and Metaschema content authors to work with related content.

This tool is based on the [Metaschema Java Tools](https://github.com/metaschema-framework/metaschema-java) and [OSCAL Java Library](https://github.com/metaschema-framework/liboscal-java/) projects.

## Contributing to this code base

Thank you for interest in contributing to the Metaschema Java framework. For complete instructions on how to contribute code, please read through our [CONTRIBUTING.md](CONTRIBUTING.md) documentation.

## Public domain

This project is in the worldwide [public domain](LICENSE.md) and as stated in [CONTRIBUTING.md](CONTRIBUTING.md).

## Installing

### Installing pre-built Java package

1.  Make a directory to install oscal-cli and cd into it. The example below uses the directory `/opt/oscal-cli`. Use your preferred directory.

```sh
mkdir -p /opt/oscal-cli
```

2. Download the zipped oscal-cli Java package to the install directory. Download your preferred version, but we recommend [the latest stable release on the Maven Central repository](https://central.sonatype.com/artifact/dev.metaschema.oscal/oscal-cli-enhanced/). You can use [the Maven tool](https://maven.apache.org/) instead of in place of additional command line tools (i.e. `curl`; `wget`; etc.) or browser to craft URLs for a specific version.

```sh
mvn \
  org.apache.maven.plugins:maven-dependency-plugin:LATEST:copy \
  -DoutputDirectory=/opt/oscal-cli \
  -DremoteRepositories=https://repo1.maven.org/maven2 \
  -Dartifact=dev.metaschema.oscal:oscal-cli-enhanced:LATEST:zip:oscal-cli
```

3. Move, extract oscal-cli, and delete the zipped package from the install directory.

```sh
cd /opt/oscal-cli
unzip *.zip
rm *.zip
```

4. (Recommended) Add oscal-cli's directory to your path.

```sh
# temporarily add oscal-cli to your terminal's instance path
PATH=$PATH:/opt/oscal-cli/bin

# add oscal-cli to your environment (e.g., all terminals)
export PATH=$PATH:/opt/oscal-cli/bin
```

5. You can optionally add oscal-cli's directory to your path in shell profile (i.e. [`$HOME/.bashrc`](https://www.gnu.org/software/bash/manual/html_node/Bash-Startup-Files.html); [`$HOME/.zshrc`](https://zsh.sourceforge.io/Guide/zshguide02.html); etc.) to make oscal-cli permanently available.

```sh
# You do not need to use both, pick one.
# Use the command below for bash shells.
echo 'export PATH=$PATH:/opt/oscal-cli/bin' >> ~/.bashrc
# Use this command for zsh shells.
echo 'export PATH=$PATH:/opt/oscal-cli/bin' >> ~/.zshrc
```

## Running 

Run help to make sure everything works.

```sh
# if oscal-cli directory added to your path
oscal-cli --help

# if you did not add oscal-cli directory to your path
/opt/oscal-cli/bin/oscal-cli --help
```

## Building

This project can be built with [Apache Maven](https://maven.apache.org/) version 3.8.4 or greater.

The following instructions can be used to clone and build this project.

1. Clone the GitHub repository.

```bash
git clone --recurse-submodules https://github.com/metaschema-framework/oscal-cli.git 
```

2. Build the project with Maven

```bash
mvn install
```

## Relationship to prior work

The contents of this repository is based on work from the [Metaschema Java repository](https://github.com/usnistgov/oscal-cli/) maintained by the National Institute of Standards and Technology (NIST), the [contents of which have been dedicated in the worldwide public domain](https://github.com/usnistgov/oscal-cli/blob/1d4f38d6b73ec34469063e2a90be69367f8d8996/LICENSE.md) using the [CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/) public domain dedication. This repository builds on this prior work, maintaining the [CCO license](https://github.com/metaschema-framework/oscal-cli/blob/main/LICENSE.md) on any new works in this repository.

This tool is maintained to be feature compatible with the NIST [v1.0.3](https://github.com/usnistgov/oscal-cli/releases/tag/v1.0.3) release. As a result, Metaschema-based and OSCAL content produced for either will work with this tool. This library version fixes bugs in in the NIST version that were confirmed to be present in this library. This has been noted in related [issues](https://github.com/usnistgov/oscal-cli/issues) in the NIST repository. Unlike its predecessor, this version supports newer releases of the OSCAL models and additional features.
