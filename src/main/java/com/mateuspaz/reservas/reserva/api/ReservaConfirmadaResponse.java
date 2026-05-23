package com.mateuspaz.reservas.reserva.api;

import java.util.UUID;

public record ReservaConfirmadaResponse(
        UUID reservaId,
        UUID vagaId,
        UUID clienteId,
        UUID requestId,
        String status) {
}
