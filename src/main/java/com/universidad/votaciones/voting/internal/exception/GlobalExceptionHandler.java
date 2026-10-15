package com.universidad.votaciones.voting.internal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VotanteYaVotoException.class)
    public ResponseEntity<Map<String, Object>> handleVotanteYaVoto(VotanteYaVotoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "VOTO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(CandidatoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCandidatoNoEncontrado(CandidatoNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "CANDIDATO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(IdempotenciaEnConflictoException.class)
    public ResponseEntity<Map<String, Object>> handleIdempotenciaEnConflicto(IdempotenciaEnConflictoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "IDEMPOTENCIA_EN_PROCESO", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String codigo, String detalle) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "codigo", codigo,
                "mensaje", detalle != null ? detalle : ""
        ));
    }
}
