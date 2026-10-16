package com.universidad.votaciones.voting.internal.dto;

import java.util.Map;

public record ConteoVotosDto(
        long totalVotos,
        Map<String, Long> votosPorCandidato
) {
}
