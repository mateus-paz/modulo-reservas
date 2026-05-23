package com.mateuspaz.reservas.reserva.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

class ReservasOpenApiContractTest {

    private final YAMLMapper yamlMapper = new YAMLMapper();

    @Test
    void definesCurrentResponsesAndFutureRequestIdConflictWithoutPromisingExecution() throws IOException {
        JsonNode contract = yamlMapper.readTree(Path.of(
                "specs/001-solicitacao-reserva-idempotente/contracts/reservas.openapi.yaml").toFile());
        JsonNode post = contract.path("paths").path("/reservas").path("post");

        assertThat(post.path("responses").fieldNames()).toIterable().containsExactlyInAnyOrder("201", "400", "409");
        assertThat(post.path("description").asText())
                .contains("Qualquer reapresentacao de requestId permanece fora do comportamento executavel");
        assertThat(post.path("responses").path("409").path("content").path("application/json")
                .path("examples").path("requestIdInconsistente").path("value").path("code").asText())
                .isEqualTo("REQUEST_ID_INCONSISTENTE");
        assertThat(StreamSupport.stream(contract.path("components").path("schemas").path("ConflictErrorResponse")
                .path("properties").path("code").path("enum").spliterator(), false)
                .map(JsonNode::asText)
                .toList())
                .contains("VAGA_EM_CONFLITO", "REQUEST_ID_INCONSISTENTE");
    }
}
