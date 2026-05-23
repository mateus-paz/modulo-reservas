package com.mateuspaz.reservas.reserva.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.mateuspaz.reservas.reserva.persistence.VagaEntity;
import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class SolicitacaoReservaSuccessContractTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsCreatedConfirmationForValidRequest() throws Exception {
        UUID vagaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        vagaRepository.save(VagaEntity.livre(vagaId));

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vagaId": "%s",
                                  "clienteId": "%s",
                                  "requestId": "%s"
                                }
                                """.formatted(vagaId, clienteId, requestId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservaId").isNotEmpty())
                .andExpect(jsonPath("$.vagaId").value(vagaId.toString()))
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }
}
