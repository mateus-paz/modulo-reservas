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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitacaoReservaValidationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsValidationErrorForMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[?(@.field == 'vagaId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'clienteId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'requestId')]").exists());
    }

    @Test
    void returnsValidationErrorForMalformedUuidAndEchoesValidRequestId() throws Exception {
        UUID requestId = UUID.randomUUID();

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vagaId": "not-a-uuid",
                                  "clienteId": "%s",
                                  "requestId": "%s"
                                }
                                """.formatted(UUID.randomUUID(), requestId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("vagaId"));
    }
}
