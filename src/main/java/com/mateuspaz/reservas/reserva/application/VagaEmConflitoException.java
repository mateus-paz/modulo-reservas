package com.mateuspaz.reservas.reserva.application;

import java.util.UUID;

public class VagaEmConflitoException extends RuntimeException {

    private final UUID requestId;

    public VagaEmConflitoException(UUID requestId) {
        super("A vaga solicitada nao esta disponivel para confirmacao.");
        this.requestId = requestId;
    }

    public UUID getRequestId() {
        return requestId;
    }
}
