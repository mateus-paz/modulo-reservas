package com.mateuspaz.reservas.reserva.persistence;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultado_requisicao")
public class ResultadoRequisicaoEntity {

    @Id
    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "vaga_id", nullable = false)
    private UUID vagaId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoSolicitacao resultado;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", nullable = false, columnDefinition = "jsonb")
    private String responseBody;

    @Column(name = "reserva_id")
    private UUID reservaId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResultadoRequisicaoEntity() {
    }

    public ResultadoRequisicaoEntity(
            UUID requestId,
            UUID vagaId,
            UUID clienteId,
            ResultadoSolicitacao resultado,
            int httpStatus,
            String responseBody,
            UUID reservaId,
            Instant createdAt) {
        this.requestId = requestId;
        this.vagaId = vagaId;
        this.clienteId = clienteId;
        this.resultado = resultado;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.reservaId = reservaId;
        this.createdAt = createdAt;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public ResultadoSolicitacao getResultado() {
        return resultado;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public UUID getReservaId() {
        return reservaId;
    }
}
