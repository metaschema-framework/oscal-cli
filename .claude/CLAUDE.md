# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build and install locally
mvn install

# Replicate CI/CD build (recommended before pushing)
mvn install -PCI -Prelease

# Run tests only
mvn test

# Run a single test class
mvn test -Dtest=CLITest

# Run a single test method
mvn test -Dtest=CLITest#testHelp

# Skip tests during build
mvn install -DskipTests

# Check license headers
mvn license:check

# Auto-format source code
mvn formatter:format

# Check for Checkstyle issues
mvn checkstyle:check
```

### Maven Profiles

- `-Prelease`: Enables release configuration (GPG signing, source/javadoc jars)
- `-PCI`: Enables CI-specific configuration
- `-Psnapshots`: Enables snapshot deployment to the snapshot repository

## Project Overview

oscal-cli is a command-line tool for working with [OSCAL](https://pages.nist.gov/OSCAL/) and [Metaschema](https://github.com/metaschema-framework/metaschema) content. It provides:

- Converting OSCAL content between XML, JSON, and YAML formats
- Validating OSCAL resources for well-formedness and validity
- Resolving OSCAL Profiles
- Validating Metaschema model definitions
- Generating XML and JSON Schemas from Metaschema models

This tool is built on [Metaschema Java Tools](https://github.com/metaschema-framework/metaschema-java) and [liboscal-java](https://github.com/metaschema-framework/liboscal-java/).

## Architecture

This is a single-module Maven project producing the `oscal-cli-enhanced` artifact.

### Package Structure

- `dev.metaschema.oscal.tools.cli.core` - CLI entry point (`CLI.java` main class)
- `dev.metaschema.oscal.tools.cli.core.commands` - Top-level commands (validate, convert, resolve-profile)
- `dev.metaschema.oscal.tools.cli.core.commands.<model>` - Model-specific subcommands (catalog, profile, ssp, etc.)
- `dev.metaschema.oscal.tools.cli.core.utils` - Utility classes

### CLI Framework

The CLI uses the `cli-processor` module from metaschema-java for command parsing and execution.

### Command Pattern

Commands follow an inheritance hierarchy:
- **Parent commands** (e.g., `CatalogCommand`) extend `AbstractParentCommand` and register subcommands
- **Validation commands** extend `AbstractOscalValidationCommand` → `AbstractValidateContentCommand`
- **Convert commands** extend `AbstractOscalConvertCommand`
- **Resolve commands** extend `AbstractResolveCommand`

Each OSCAL model type (catalog, profile, ssp, component-definition, assessment-plan, assessment-results, poam) has its own package with validate and convert subcommands.

## Code Style

- Java 11 target
- Uses SpotBugs annotations (`@NonNull`, `@Nullable`) for null safety

## Git Workflow

- Repository: https://github.com/metaschema-framework/oscal-cli
- **All PRs MUST be created from a personal fork** (required by CONTRIBUTING.md)
- **All PRs MUST target the `develop` branch**
- All changes require PR review

## Git Worktrees (MANDATORY)

**All development work MUST be done in a git worktree, not in the main repository checkout.**

### Why Worktrees Are Required

- Isolates feature work from the main checkout
- Prevents accidental commits to the wrong branch
- Allows parallel work on multiple features
- Keeps the main checkout clean for reference and review

### Worktree Location

Worktrees are stored in `.worktrees/` directory (gitignored) relative to the repository root.

### Workflow

1. **Before starting any feature work**, create a worktree:

```bash
# Create worktree for a new feature branch
git worktree add .worktrees/<feature-name> -b <feature-branch> origin/develop
```

2. **Check for existing worktrees** before making changes:

```bash
git worktree list
```

3. **Switch to the appropriate worktree** if one already exists for your task

4. **Remove worktrees** after PRs are merged:

```bash
git worktree remove .worktrees/<feature-name>
```

### Red Flags (You're Working in the Wrong Directory)

- Making changes without first checking `git worktree list`
- Working in the main repository when a worktree exists for the feature
- Creating files or commits in the main checkout for feature work

Use the `superpowers:using-git-worktrees` skill for guided worktree creation and management.

## Testing

- Tests use JUnit 5
- All PRs require passing CI checks before merge
- 100% of unit tests must pass before pushing code
- See [TESTING.md](TESTING.md) for detailed testing documentation

## Dependencies

This tool depends on:
- `metaschema-java` (core Metaschema framework, including cli-processor)
- `liboscal-java` (OSCAL Java library)

## Running the CLI

After building, run the CLI:

```bash
# From the build output
target/oscal-cli/bin/oscal-cli --help

# Disable colored output for legacy terminals
oscal-cli --no-color <command>
```

## Available Skills

### Metaschema (metaschema plugin)
- `metaschema:metaschema-basics` - Introduction to Metaschema concepts
- `metaschema:metaschema-module-authoring` - Creating/modifying Metaschema modules
- `metaschema:metaschema-constraints-authoring` - Writing Metaschema constraints
- `metaschema:metapath-expressions` - Metapath query syntax and functions

### OSCAL (oscal plugin)
- `oscal:oscal-basics` - Working with OSCAL documents
- `oscal:oscal-catalog` - OSCAL catalog documents
- `oscal:oscal-profile` - OSCAL profiles and baselines
- `oscal:oscal-ssp` - System Security Plans
- `oscal:oscal-component-definition` - Component definitions

### Development (dev-metaschema plugin)
- `dev-metaschema:development-workflow` - TDD, debugging, PRD workflow
- `dev-metaschema:javadoc-style-guide` - Javadoc requirements and patterns
- `dev-metaschema:unit-test-writing` - Test patterns and edge cases

### Tools
- `metaschema-tools:using-metaschema-java` - metaschema-java CLI commands
- `oscal-tools:using-oscal-cli` - oscal-cli commands and usage
