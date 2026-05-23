package com.mateuspaz.reservas.reserva.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mateuspaz.reservas.support.PostgresIntegrationTest;

@SpringBootTest
@ActiveProfiles("integration")
class ReservaConfirmationConstraintIntegrationTest extends PostgresIntegrationTest {

    @Test
    void preventsTwoConfirmedReservationsForSameVaga() {
        UUID vagaId = UUID.randomUUID();
        vagaRepository.save(VagaEntity.livre(vagaId));
        reservaRepository.saveAndFlush(ReservaEntity.confirmada(vagaId, UUID.randomUUID(), UUID.randomUUID()));

        assertThatThrownBy(() -> reservaRepository.saveAndFlush(
                ReservaEntity.confirmada(vagaId, UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
