---
title: "Maven: Java compiler version incompatibilities"
technology: MAVEN
version: "Maven 3.9; Java 21"
language: en
doc_type: runbook
source_url: "https://maven.apache.org/plugins/maven-compiler-plugin/"
---
# Maven: Java compiler version incompatibilities

## Symptoms

- invalid target release
- release version is not supported
- source option is no longer supported
- class file has wrong version

## Common causes

1. Maven uses a different JDK from the terminal.
2. The compiler release is newer than the installed JDK.
3. JAVA_HOME points to an unexpected installation.
4. The compiler plugin configuration is inconsistent.

## Checks

- Run `java -version`.
- Run `mvn -version` and inspect the Java version it reports.
- Inspect JAVA_HOME.
- Review the maven-compiler-plugin configuration.
- Check the configured source, target or release value.

## General resolution

Align the JDK used by Maven with the project's supported Java version. Prefer a consistent release configuration and verify the build in a clean environment.
