package com.mateuspaz.reservas.reserva.api;

import java.util.UUID;

public record ConflictErrorResponse(UUID requestId, String code, String message) {
}
