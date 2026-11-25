---
title: "GitHub Actions: workflow syntax failures"
technology: GITHUB_ACTIONS
version: "GitHub Actions"
language: en
doc_type: runbook
source_url: "https://docs.github.com/en/actions"
---
# GitHub Actions: workflow syntax failures

## Symptoms

- Invalid workflow file
- Unexpected value
- Unrecognized named-value
- Required property is missing
- Workflow is not triggered

## Common causes

1. YAML indentation is invalid.
2. A key is placed at the wrong level.
3. An expression uses invalid syntax.
4. A job references a missing dependency.
5. The workflow file is outside the expected directory.

## Checks

- Inspect the workflow under .github/workflows.
- Validate indentation and mapping structure.
- Check the official workflow syntax reference.
- Review job dependencies and expression contexts.
- Confirm the event trigger configuration.

## General resolution

Correct the YAML structure and expression syntax, then validate the workflow again. Check the exact line and key reported by the workflow parser.
