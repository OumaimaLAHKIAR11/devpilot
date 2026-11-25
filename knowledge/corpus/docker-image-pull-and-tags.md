---
title: "Docker: image pull and tag failures"
technology: DOCKER
version: "Docker Engine"
language: en
doc_type: runbook
source_url: "https://docs.docker.com/reference/cli/docker/image/pull/"
---
# Docker: image pull and tag failures

## Symptoms

- pull access denied
- manifest unknown
- repository does not exist
- requested access to the resource is denied
- unauthorized

## Common causes

1. The image repository or tag is misspelled.
2. The requested tag has not been published.
3. The registry requires authentication.
4. The account lacks permission to access a private image.
5. The image name omits the correct registry namespace.

## Checks

- Verify the complete image name and tag.
- Inspect the registry hostname and namespace.
- Check whether the image is public or private.
- Authenticate to the expected registry.
- Confirm that the requested tag exists.

## General resolution

Correct the repository reference or tag and authenticate with an account that has the required permissions. Never put registry passwords directly in a Dockerfile.
