
# Knowledge Engine — Decision 0

## Scope

The initial Knowledge Engine uses a Markdown corpus of
technical runbooks and a JSONL Golden Set for retrieval evaluation.

## Embeddings

- Provider: Azure OpenAI
- Model: text-embedding-3-small
- Vector dimension: 1536

## Persistence

- Database: PostgreSQL
- Vector extension: pgvector
- Schema managed by Flyway

## Retrieval contract

The application exposes a KnowledgeSearch abstraction.
The initial fake implementation is used for contract tests.
Real vector retrieval is implemented in a later step.

## Evaluation

- Golden Set format: JSONL
- Pilot size: 20 cases
- Initial source: LogChunks
- Relevance annotations reference the 10 current runbooks.
- Unanswerable cases are retained.

## Scope boundaries

Document ingestion is implemented in Step 1.
Chunking and bulk embedding generation are implemented
in Step 2.
Vector and hybrid retrieval are implemented later.

## Known limitations

The pilot Golden Set is small and initially Java-focused.
It must be expanded and diversified during evaluation.
