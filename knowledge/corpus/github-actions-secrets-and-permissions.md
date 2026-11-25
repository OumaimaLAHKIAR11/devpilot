---
title: "GitHub Actions: secrets and token permissions"
technology: GITHUB_ACTIONS
version: "GitHub Actions"
language: en
doc_type: runbook
source_url: "https://docs.github.com/en/actions/security-for-github-actions"
---
# GitHub Actions: secrets and token permissions

## Symptoms

- Resource not accessible by integration
- Bad credentials
- Secret is empty
- Permission denied
- API returns HTTP 403

## Common causes

1. A required repository or environment secret is not configured.
2. The workflow references the wrong secret name.
3. The GITHUB_TOKEN has insufficient permissions.
4. An environment protection rule prevents access.
5. A forked pull request does not receive certain secrets.

## Checks

- Verify the secret name in repository or environment settings.
- Check the workflow expression referencing the secret.
- Inspect the permissions block.
- Review environment protection rules.
- Identify whether the workflow runs from a fork.

## General resolution

Configure the required secret in the correct scope and grant only the minimum token permissions required. Do not print secret values in logs or expose them in workflow outputs.
