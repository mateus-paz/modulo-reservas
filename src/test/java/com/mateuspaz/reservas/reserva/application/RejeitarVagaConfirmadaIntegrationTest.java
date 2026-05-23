package com.mateuspaz.reservas.reserva.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mateuspaz.reservas.reserva.api.SolicitacaoReservaRequest;
import com.mateuspaz.reservas.reserva.persistence.ResultadoSolicitacao;
import com.mateuspaz.reservas.reserva.persistence.VagaEntity;
import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@ActiveProfiles("integration")
class RejeitarVagaConfirmadaIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private SolicitacaoReservaService service;

    @Test
    void rejectsAlreadyConfirmedVagaWithoutAdditionalReservation() {
        UUID vagaId = UUID.randomUUID();
        vagaRepository.save(VagaEntity.livre(vagaId));
        service.solicitar(new SolicitacaoReservaRequest(
                vagaId.toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()));
        UUID rejectedRequestId = UUID.randomUUID();

        assertThatThrownBy(() -> service.solicitar(new SolicitacaoReservaRequest(
                vagaId.toString(),
                UUID.randomUUID().toString(),
                rejectedRequestId.toString())))
                .isInstanceOf(VagaEmConflitoException.class);

        assertThat(reservaRepository.findAll()).hasSize(1);
        assertThat(resultadoRequisicaoRepository.findById(rejectedRequestId).orElseThrow().getResultado())
                .isEqualTo(ResultadoSolicitacao.CONFLITO_VAGA);
    }
}
