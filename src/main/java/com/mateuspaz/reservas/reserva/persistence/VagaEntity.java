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
@Table(name = "vaga")
public class VagaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VagaStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VagaEntity() {
    }

    public VagaEntity(UUID id, VagaStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static VagaEntity livre(UUID id) {
        Instant now = Instant.now();
        return new VagaEntity(id, VagaStatus.LIVRE, now, now);
    }

    public void confirmar() {
        this.status = VagaStatus.CONFIRMADA;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public VagaStatus getStatus() {
        return status;
    }
}
