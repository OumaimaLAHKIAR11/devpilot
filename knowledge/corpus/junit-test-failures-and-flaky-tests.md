---
title: "Testing: JUnit failures and flaky tests"
technology: TESTING
version: "JUnit 5"
language: en
doc_type: runbook
source_url: "https://junit.org/junit5/docs/current/user-guide/"
---
# Testing: JUnit failures and flaky tests

## Symptoms

- Tests pass locally but fail in CI.
- Tests fail intermittently.
- AssertionFailedError
- TimeoutException
- Tests depend on execution order.

## Common causes

1. Tests share mutable state.
2. A test depends on network or external service availability.
3. Timing assumptions are too strict.
4. Test data is not isolated or cleaned up.
5. Tests rely on execution order or machine-specific paths.

## Checks

- Identify the first failing assertion.
- Run the failing test independently.
- Inspect setup and teardown methods.
- Check shared resources and parallel execution.
- Review timeouts, clocks and external dependencies.

## General resolution

Make tests deterministic and independent. Use controlled fixtures, isolate external services and clean up test data. Do not hide intermittent failures by repeatedly rerunning the entire test suite without investigating their cause.
