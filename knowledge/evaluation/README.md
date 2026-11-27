
# LOGMIND — Golden Set RAG v1

## Purpose

This dataset evaluates the retrieval quality of the LOGMIND
Knowledge Engine.

## Dataset

- Version: v1
- Target size: 20 cases
- Main source: LogChunks
- Initial focus: Java build failures

## Annotation policy

Each case contains:
- A real build failure excerpt.
- A diagnostic query.
- Technology metadata.
- Relevant runbook identifiers.
- An answerability judgment.
- An annotation rationale.

## Answerability

- `true`: at least one current corpus document provides
  useful evidence for the query.
- `false`: the current corpus does not provide sufficient
  knowledge to answer the query reliably.

## Relevance

Relevant documents must be selected from the current
10-document corpus.

## Limitations

The initial set is a pilot and is not statistically
representative of all software build failures.

Annotations derived from LogChunks must be checked
against the original logs.
