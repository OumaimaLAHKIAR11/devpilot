
package ma.ensias.devpilot.knowledge;

import ma.ensias.devpilot.knowledge.internal.persistence.PgVectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ma.ensias.devpilot.knowledge.internal.embedding.FakeTextEmbedder;
import ma.ensias.devpilot.knowledge.internal.embedding.TextEmbedder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Tag("integration")
@Import(PgVectorSchemaIT.FakeEmbeddingConfiguration.class)
class PgVectorSchemaIT {

    @Container
    static PostgreSQLContainer pg = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pg::getJdbcUrl);
        registry.add("spring.datasource.username", pg::getUsername);
        registry.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired
    JdbcClient jdbc;

    @Test
    void flywayCreatedTheKnowledgeTables() {
        Integer count = jdbc.sql("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN (
                    'knowledge_document',
                    'knowledge_ingestion_job',
                    'document_chunk'
                  )
                """)
                .query(Integer.class)
                .single();

        assertThat(count).isEqualTo(3);
    }

    @Test
    void nearestNeighbourFollowsCosineDistance() {
        UUID documentId = insertDocument("a");

        UUID near = insertChunk(
                documentId, 0, unit(0, 0.9f, 1, 0.1f)
        );

        UUID far = insertChunk(
                documentId, 1, unit(5, 1.0f, 6, 0.0f)
        );

        List<UUID> ranked = jdbc.sql("""
                SELECT id
                FROM document_chunk
                ORDER BY embedding <=> CAST(:query AS vector)
                LIMIT 2
                """)
                .param("query", PgVectors.literal(
                        unit(0, 1.0f, 1, 0.0f)
                ))
                .query(UUID.class)
                .list();

        assertThat(ranked).containsExactly(near, far);
    }

    @Test
    void wrongDimensionIsRejected() {
        UUID documentId = insertDocument("b");

        assertThatThrownBy(() -> jdbc.sql("""
                INSERT INTO document_chunk (
                    id, document_id, chunk_index, content,
                    embedding, embedding_model,
                    embedding_dimension, chunking_version
                )
                VALUES (
                    :id, :doc, 0, 'x',
                    CAST('[1,2,3]' AS vector),
                    'test', 3, 'v1'
                )
                """)
                .param("id", UUID.randomUUID())
                .param("doc", documentId)
                .update())
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
    }

    @Test
    void deletingADocumentDeletesItsChunks() {
        UUID documentId = insertDocument("c");

        insertChunk(
                documentId, 0, unit(0, 1.0f, 1, 0.0f)
        );

        jdbc.sql("""
                DELETE FROM knowledge_document WHERE id = :id
                """)
                .param("id", documentId)
                .update();

        Integer count = jdbc.sql("""
                SELECT count(*)
                FROM document_chunk
                WHERE document_id = :id
                """)
                .param("id", documentId)
                .query(Integer.class)
                .single();

        assertThat(count).isZero();
    }

    @Test
    void sameHashCannotBeActiveTwice() {
        insertDocument("d");

        assertThatThrownBy(() -> insertDocument("d"))
                .isInstanceOf(
                        org.springframework.dao.DataAccessException.class
                );
    }

    private UUID insertDocument(String hashSuffix) {
        UUID id = UUID.randomUUID();

        String hash = hashSuffix.repeat(64);

        jdbc.sql("""
                INSERT INTO knowledge_document (
                    id, title, technology, doc_type,
                    original_filename, content_hash,
                    size_bytes, status
                )
                VALUES (
                    :id, 'Test document', 'MAVEN',
                    'MARKDOWN', 'test.md', :hash,
                    10, 'EXTRACTED'
                )
                """)
                .param("id", id)
                .param("hash", hash)
                .update();

        return id;
    }

    private UUID insertChunk(
            UUID documentId,
            int index,
            float[] vector
    ) {
        UUID id = UUID.randomUUID();

        jdbc.sql("""
                INSERT INTO document_chunk (
                    id, document_id, chunk_index, content,
                    embedding, embedding_model,
                    embedding_dimension, chunking_version
                )
                VALUES (
                    :id, :doc, :idx, 'test content',
                    CAST(:embedding AS vector),
                    'fake-embedding', 1536, 'v1'
                )
                """)
                .param("id", id)
                .param("doc", documentId)
                .param("idx", index)
                .param("embedding", PgVectors.literal(vector))
                .update();

        return id;
    }

    private static float[] unit(
            int firstIndex,
            float firstValue,
            int secondIndex,
            float secondValue
    ) {
        float[] vector = new float[1536];

        vector[firstIndex] = firstValue;
        vector[secondIndex] = secondValue;

        return vector;
    }

    @TestConfiguration
    static class FakeEmbeddingConfiguration {

        @Bean
        TextEmbedder textEmbedder() {
            return new FakeTextEmbedder();
        }
    }
}
