package com.mateuspaz.reservas.reserva.api;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mateuspaz.reservas.reserva.application.VagaEmConflitoException;

@RestControllerAdvice
public class ReservaExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<ApiErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorDetail(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiErrorDetail::field).thenComparing(ApiErrorDetail::message))
                .toList();
        ValidationErrorResponse response = new ValidationErrorResponse(
                validRequestId(exception.getBindingResult().getTarget()),
                "VALIDATION_ERROR",
                "A solicitacao contem dados invalidos.",
                errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(VagaEmConflitoException.class)
    public ResponseEntity<ConflictErrorResponse> handleVagaConflict(VagaEmConflitoException exception) {
        ConflictErrorResponse response = new ConflictErrorResponse(
                exception.getRequestId(),
                "VAGA_EM_CONFLITO",
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private UUID validRequestId(Object target) {
        if (!(target instanceof SolicitacaoReservaRequest request) || request.requestId() == null) {
            return null;
        }
        try {
            return UUID.fromString(request.requestId());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
