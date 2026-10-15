package com.universidad.votaciones.voting.internal.dto;

import java.time.LocalDateTime;

public record VotoResult(
        String mensaje,
        String llaveIdempotencia,
        String tokenAnonimo,
        LocalDateTime fechaHoraRegistro,
        String estado,
        boolean esIdempotente
) {
}
