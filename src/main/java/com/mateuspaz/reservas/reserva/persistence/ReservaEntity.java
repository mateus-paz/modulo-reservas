package com.mateuspaz.reservas.reserva.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva")
public class ReservaEntity {

    @Id
    private UUID id;

    @Column(name = "vaga_id", nullable = false)
    private UUID vagaId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservaStatus status;

    @Column(name = "confirmed_at", nullable = false)
    private Instant confirmedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReservaEntity() {
    }

    public ReservaEntity(
            UUID id,
            UUID vagaId,
            UUID clienteId,
            UUID requestId,
            ReservaStatus status,
            Instant confirmedAt,
            Instant createdAt) {
        this.id = id;
        this.vagaId = vagaId;
        this.clienteId = clienteId;
        this.requestId = requestId;
        this.status = status;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
    }

    public static ReservaEntity confirmada(UUID vagaId, UUID clienteId, UUID requestId) {
        Instant now = Instant.now();
        return new ReservaEntity(UUID.randomUUID(), vagaId, clienteId, requestId, ReservaStatus.CONFIRMADA, now, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public ReservaStatus getStatus() {
        return status;
    }
}
