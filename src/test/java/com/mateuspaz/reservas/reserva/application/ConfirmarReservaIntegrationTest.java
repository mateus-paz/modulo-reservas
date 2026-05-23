package com.mateuspaz.reservas.reserva.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mateuspaz.reservas.reserva.api.ReservaConfirmadaResponse;
import com.mateuspaz.reservas.reserva.api.SolicitacaoReservaRequest;
import com.mateuspaz.reservas.reserva.persistence.ResultadoSolicitacao;
import com.mateuspaz.reservas.reserva.persistence.VagaEntity;
import com.mateuspaz.reservas.reserva.persistence.VagaStatus;
import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@ActiveProfiles("integration")
class ConfirmarReservaIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SolicitacaoReservaService service;

    @Test
    void confirmsFreeVagaAndPersistsReservationResult() {
        UUID vagaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        vagaRepository.save(VagaEntity.livre(vagaId));

        ReservaConfirmadaResponse response = service.solicitar(new SolicitacaoReservaRequest(
                vagaId.toString(),
                clienteId.toString(),
                requestId.toString()));

        assertThat(response.status()).isEqualTo("CONFIRMADA");
        assertThat(response.vagaId()).isEqualTo(vagaId);
        assertThat(response.clienteId()).isEqualTo(clienteId);
        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(reservaRepository.findAll()).hasSize(1);
        assertThat(vagaRepository.findById(vagaId).orElseThrow().getStatus()).isEqualTo(VagaStatus.CONFIRMADA);
        assertThat(resultadoRequisicaoRepository.findById(requestId).orElseThrow().getResultado())
                .isEqualTo(ResultadoSolicitacao.CONFIRMADA);
    }
}
