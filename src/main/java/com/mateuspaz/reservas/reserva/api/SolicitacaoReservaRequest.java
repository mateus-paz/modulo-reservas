package com.mateuspaz.reservas.reserva.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitacaoReservaRequest(
        @NotBlank(message = "vagaId is required")
        @Pattern(regexp = UUID_PATTERN, message = "vagaId must be a valid UUID")
        String vagaId,

        @NotBlank(message = "clienteId is required")
        @Pattern(regexp = UUID_PATTERN, message = "clienteId must be a valid UUID")
        String clienteId,

        @NotBlank(message = "requestId is required")
        @Pattern(regexp = UUID_PATTERN, message = "requestId must be a valid UUID")
        String requestId) {

    static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
}
