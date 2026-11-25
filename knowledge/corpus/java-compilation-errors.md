---
title: "Java: compilation errors and unresolved symbols"
technology: JAVA
version: "Java 21"
language: en
doc_type: runbook
source_url: "https://docs.oracle.com/en/java/"
---
# Java: compilation errors and unresolved symbols

## Symptoms

- cannot find symbol
- incompatible types
- package does not exist
- method cannot be applied to given types
- class file has wrong version

## Common causes

1. A class or method name is incorrect.
2. An import is missing or references the wrong package.
3. A dependency is missing from the classpath.
4. Method arguments do not match the declared signature.
5. The compiler uses an incompatible JDK or language level.

## Checks

- Read the first compiler error and its source location.
- Verify the declaration and imports.
- Inspect Maven or Gradle dependencies.
- Check method signatures and generic types.
- Verify the JDK used by the build.

## General resolution

Correct the declaration, import, dependency or type mismatch identified by the compiler. Rebuild after addressing the earliest relevant error because later errors may be consequences of it.
