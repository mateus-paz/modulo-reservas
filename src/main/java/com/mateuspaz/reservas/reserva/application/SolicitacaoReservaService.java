package com.mateuspaz.reservas.reserva.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateuspaz.reservas.reserva.api.ReservaConfirmadaResponse;
import com.mateuspaz.reservas.reserva.api.SolicitacaoReservaRequest;
import com.mateuspaz.reservas.reserva.persistence.ReservaEntity;
import com.mateuspaz.reservas.reserva.persistence.ReservaRepository;
import com.mateuspaz.reservas.reserva.persistence.ResultadoRequisicaoEntity;
import com.mateuspaz.reservas.reserva.persistence.ResultadoRequisicaoRepository;
import com.mateuspaz.reservas.reserva.persistence.ResultadoSolicitacao;
import com.mateuspaz.reservas.reserva.persistence.VagaEntity;
import com.mateuspaz.reservas.reserva.persistence.VagaRepository;
import com.mateuspaz.reservas.reserva.persistence.VagaStatus;

@Service
public class SolicitacaoReservaService {

    private static final String CONFLICT_MESSAGE = "A vaga solicitada nao esta disponivel para confirmacao.";

    private final VagaRepository vagaRepository;
    private final ReservaRepository reservaRepository;
    private final ResultadoRequisicaoRepository resultadoRequisicaoRepository;
    private final ObjectMapper objectMapper;

    public SolicitacaoReservaService(
            VagaRepository vagaRepository,
            ReservaRepository reservaRepository,
            ResultadoRequisicaoRepository resultadoRequisicaoRepository,
            ObjectMapper objectMapper) {
        this.vagaRepository = vagaRepository;
        this.reservaRepository = reservaRepository;
        this.resultadoRequisicaoRepository = resultadoRequisicaoRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(noRollbackFor = VagaEmConflitoException.class)
    public ReservaConfirmadaResponse solicitar(SolicitacaoReservaRequest request) {
        UUID vagaId = UUID.fromString(request.vagaId());
        UUID clienteId = UUID.fromString(request.clienteId());
        UUID requestId = UUID.fromString(request.requestId());

        VagaEntity vaga = vagaRepository.findById(vagaId).orElse(null);
        if (vaga == null || vaga.getStatus() != VagaStatus.LIVRE) {
            resultadoRequisicaoRepository.save(new ResultadoRequisicaoEntity(
                    requestId,
                    vagaId,
                    clienteId,
                    ResultadoSolicitacao.CONFLITO_VAGA,
                    409,
                    conflictBody(requestId),
                    null,
                    Instant.now()));
            throw new VagaEmConflitoException(requestId);
        }

        vaga.confirmar();
        ReservaEntity reserva = reservaRepository.save(ReservaEntity.confirmada(vagaId, clienteId, requestId));
        ReservaConfirmadaResponse response = new ReservaConfirmadaResponse(
                reserva.getId(),
                vagaId,
                clienteId,
                requestId,
                "CONFIRMADA");

        resultadoRequisicaoRepository.save(new ResultadoRequisicaoEntity(
                requestId,
                vagaId,
                clienteId,
                ResultadoSolicitacao.CONFIRMADA,
                201,
                toJson(response),
                reserva.getId(),
                Instant.now()));
        return response;
    }

    private String conflictBody(UUID requestId) {
        return """
                {"requestId":"%s","code":"VAGA_EM_CONFLITO","message":"%s"}
                """.formatted(requestId, CONFLICT_MESSAGE).trim();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to persist reservation result response", exception);
        }
    }
}
