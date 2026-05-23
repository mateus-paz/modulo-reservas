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

import com.mateuspaz.reservas.reserva.application.SolicitacaoReservaService;
import com.mateuspaz.reservas.reserva.persistence.VagaEntity;
import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
class SolicitacaoReservaConflictContractTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitacaoReservaService service;

    @Test
    void returnsVagaConflictForPreviouslyConfirmedVaga() throws Exception {
        UUID vagaId = UUID.randomUUID();
        vagaRepository.save(VagaEntity.livre(vagaId));
        service.solicitar(new SolicitacaoReservaRequest(
                vagaId.toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()));
        UUID requestId = UUID.randomUUID();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vagaId": "%s",
                                  "clienteId": "%s",
                                  "requestId": "%s"
                                }
                                """.formatted(vagaId, UUID.randomUUID(), requestId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.code").value("VAGA_EM_CONFLITO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
