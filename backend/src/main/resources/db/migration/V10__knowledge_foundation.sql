-- The vector extension is created in V1__init.sql

CREATE TABLE knowledge_document (
                                    id                UUID PRIMARY KEY,
                                    title             VARCHAR(300) NOT NULL,
                                    source_uri        VARCHAR(1000),
                                    doc_version       VARCHAR(100),
                                    technology        VARCHAR(30) NOT NULL,
                                    doc_type          VARCHAR(20) NOT NULL
                                        CHECK (doc_type IN ('MARKDOWN', 'TEXT', 'PDF')),
                                    language          VARCHAR(10),
                                    original_filename VARCHAR(255) NOT NULL,
                                    content_hash      CHAR(64) NOT NULL,
                                    size_bytes        BIGINT NOT NULL CHECK (size_bytes > 0),
                                    extracted_text    TEXT,
                                    extracted_chars   INTEGER NOT NULL DEFAULT 0
                                        CHECK (extracted_chars >= 0),
                                    status            VARCHAR(20) NOT NULL
                                        CHECK (status IN ('RECEIVED', 'EXTRACTED', 'INDEXED', 'FAILED')),
                                    active            BOOLEAN NOT NULL DEFAULT TRUE,
                                    supersedes_id     UUID REFERENCES knowledge_document (id),
                                    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A given content can be active only once (duplicate detection)
CREATE UNIQUE INDEX uq_knowledge_document_hash_active
    ON knowledge_document (content_hash) WHERE active;

CREATE INDEX idx_knowledge_document_filename
    ON knowledge_document (original_filename) WHERE active;

CREATE INDEX idx_knowledge_document_tech
    ON knowledge_document (technology) WHERE active;


CREATE TABLE knowledge_ingestion_job (
                                         id          UUID PRIMARY KEY,
                                         document_id UUID REFERENCES knowledge_document (id) ON DELETE SET NULL,
                                         filename    VARCHAR(255) NOT NULL,
                                         status      VARCHAR(20) NOT NULL
                                             CHECK (status IN ('RUNNING', 'SUCCEEDED', 'REJECTED', 'FAILED')),
                                         error_code  VARCHAR(50),
                                         message     VARCHAR(1000),
                                         started_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         finished_at TIMESTAMPTZ
);

CREATE INDEX idx_ingestion_job_document
    ON knowledge_ingestion_job (document_id);

CREATE INDEX idx_ingestion_job_started
    ON knowledge_ingestion_job (started_at DESC);


CREATE TABLE document_chunk (
                                id                  UUID PRIMARY KEY,
                                document_id         UUID NOT NULL
                                    REFERENCES knowledge_document (id) ON DELETE CASCADE,
                                chunk_index         INTEGER NOT NULL CHECK (chunk_index >= 0),
                                section_title       VARCHAR(500),
                                content             TEXT NOT NULL,
                                token_estimate      INTEGER,
                                embedding           VECTOR(1536) NOT NULL,
                                embedding_model     VARCHAR(100) NOT NULL,
                                embedding_dimension INTEGER NOT NULL,
                                chunking_version    VARCHAR(30) NOT NULL,
                                created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                                UNIQUE (document_id, chunk_index, chunking_version)
);

CREATE INDEX idx_chunk_document
    ON document_chunk (document_id);

CREATE INDEX idx_chunk_embedding_hnsw
    ON document_chunk USING hnsw (embedding vector_cosine_ops);