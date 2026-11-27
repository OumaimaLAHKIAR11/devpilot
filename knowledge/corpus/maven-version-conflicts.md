---
title: "Maven: transitive dependency version conflicts"
technology: MAVEN
version: "Maven 3.9"
language: en
doc_type: runbook
source_url: "https://maven.apache.org/guides/"
---
# Maven: transitive dependency version conflicts

## Symptoms

- Different versions of the same library appear in the dependency tree.
- NoSuchMethodError or NoClassDefFoundError occurs at runtime.
- Compilation succeeds but application startup fails.
- A dependency behaves differently after adding another library.

## Common causes

1. Two direct dependencies request different versions of a transitive library.
2. A dependency brings an outdated transitive artifact.
3. Dependency management is inconsistent across modules.
4. A BOM is missing or imported at an incorrect scope.

## Checks

- Run `mvn dependency:tree`.
- Identify which dependency introduces each version.
- Inspect dependencyManagement and imported BOMs.
- Compare compile and runtime dependency scopes.

## General resolution

Use dependency management to establish a deliberate version policy. Exclude an unwanted transitive dependency only when its replacement is explicitly declared and compatible. Re-run tests after changing the dependency graph.
