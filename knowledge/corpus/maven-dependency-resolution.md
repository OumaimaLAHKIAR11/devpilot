---
title: "Maven: dependency resolution failures"
technology: MAVEN
version: "Maven 3.9"
language: en
doc_type: runbook
source_url: "https://maven.apache.org/guides/"
---
# Maven: dependency resolution failures

## Symptoms

Typical messages include:
- Could not resolve dependencies for project.
- Could not find artifact.
- Failed to collect dependencies.
- Could not transfer artifact.

## Common causes

1. The requested artifact or version does not exist.
2. A Maven repository is unreachable.
3. Repository credentials are missing or invalid.
4. A proxy or network configuration blocks access.
5. A cached artifact contains incomplete metadata.

## Checks

- Read the first meaningful `Caused by` message.
- Verify groupId, artifactId and version.
- Check configured repositories in the POM and settings.xml.
- Verify network access and repository authentication.
- Retry with Maven debug output when necessary.

## General resolution

Correct invalid coordinates, restore repository connectivity or credentials, and retry the build. Avoid deleting the entire local repository before identifying the failing artifact.
