---
title: "Docker: image build failures"
technology: DOCKER
version: "Docker Engine"
language: en
doc_type: runbook
source_url: "https://docs.docker.com/build/"
---
# Docker: image build failures

## Symptoms

- failed to solve
- failed to compute cache key
- COPY failed
- file not found in build context
- process exited with a non-zero code

## Common causes

1. A file referenced by COPY is outside the build context.
2. The Dockerfile uses an incorrect relative path.
3. A required file is excluded by .dockerignore.
4. A build command fails inside a RUN instruction.
5. The selected build context is incorrect.

## Checks

- Inspect the exact Docker build command and its final context argument.
- Check paths relative to the build context.
- Review .dockerignore.
- Inspect the failing Dockerfile instruction.
- Re-run with plain build progress to expose the failing step.

## General resolution

Correct the build context or file paths, restore required files excluded by .dockerignore, and isolate failing RUN instructions. Do not assume that paths are relative to the Dockerfile location.
