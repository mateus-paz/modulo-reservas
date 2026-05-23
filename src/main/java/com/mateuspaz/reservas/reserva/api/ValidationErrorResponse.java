package com.mateuspaz.reservas.reserva.api;

import java.util.List;
import java.util.UUID;

public record ValidationErrorResponse(
        UUID requestId,
        String code,
        String message,
        List<ApiErrorDetail> errors) {
}
