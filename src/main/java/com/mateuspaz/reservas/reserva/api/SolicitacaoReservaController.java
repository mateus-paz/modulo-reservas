package com.mateuspaz.reservas.reserva.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mateuspaz.reservas.reserva.application.SolicitacaoReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reservas")
public class SolicitacaoReservaController {

    private final SolicitacaoReservaService solicitacaoReservaService;

    public SolicitacaoReservaController(SolicitacaoReservaService solicitacaoReservaService) {
        this.solicitacaoReservaService = solicitacaoReservaService;
    }

    @PostMapping
    public ResponseEntity<ReservaConfirmadaResponse> solicitar(@Valid @RequestBody SolicitacaoReservaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitacaoReservaService.solicitar(request));
    }
}
