---
title: "Spring Boot: application context startup failures"
technology: SPRING
version: "Spring Boot 3.x and 4.x"
language: en
doc_type: runbook
source_url: "https://docs.spring.io/spring-boot/reference/"
---
# Spring Boot: application context startup failures

## Symptoms

- ApplicationContext failed to start
- UnsatisfiedDependencyException
- NoSuchBeanDefinitionException
- BeanCreationException
- Failed to configure a DataSource

## Common causes

1. A required bean is missing.
2. Component scanning does not include the package.
3. Two beans have conflicting definitions.
4. A required configuration property is absent.
5. A database or external service is unavailable.

## Checks

- Read the deepest relevant Caused by section.
- Inspect the bean named in the exception.
- Check component scanning and conditional annotations.
- Verify active Spring profiles and configuration properties.
- Test database connectivity independently.

## General resolution

Fix the underlying dependency or configuration issue rather than suppressing the startup exception. Re-run the application with the intended profile and verify its health endpoint.
