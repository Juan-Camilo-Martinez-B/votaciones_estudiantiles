package com.universidad.votaciones.voting.internal.dto;

public record EmitirVotoCommand(
        String llaveIdempotencia,
        String idEstudiante,
        String idCandidato
) {
    public EmitirVotoCommand {
        if (llaveIdempotencia == null || llaveIdempotencia.isBlank()) {
            throw new IllegalArgumentException("La llave de idempotencia es obligatoria.");
        }
        if (idEstudiante == null || idEstudiante.isBlank()) {
            throw new IllegalArgumentException("El identificador del estudiante es obligatorio.");
        }
        if (idCandidato == null || idCandidato.isBlank()) {
            throw new IllegalArgumentException("El identificador del candidato es obligatorio.");
        }
    }
}
