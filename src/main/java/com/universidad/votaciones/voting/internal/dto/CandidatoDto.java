package com.universidad.votaciones.voting.internal.dto;

public record CandidatoDto(
        String idCandidato,
        String numeroTarjeton,
        String nombreCandidato,
        String propuestaResumen
) {
}
