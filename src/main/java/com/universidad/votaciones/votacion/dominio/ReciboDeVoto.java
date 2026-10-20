package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDateTime;

public record ReciboDeVoto(
        String mensaje,
        String llaveIdempotencia,
        String tokenAnonimo,
        LocalDateTime fechaHoraRegistro,
        String estado,
        boolean esIdempotente) {

    public static ReciboDeVoto nuevo(Participacion participacion) {
        return new ReciboDeVoto(
                "Voto registrado exitosamente.",
                participacion.llaveIdempotencia(),
                participacion.tokenRecibo(),
                participacion.fechaHora(),
                "PROCESADO",
                false);
    }

    public static ReciboDeVoto idempotente(Participacion participacion) {
        return new ReciboDeVoto(
                "Voto previamente procesado (Respuesta Idempotente)",
                participacion.llaveIdempotencia(),
                participacion.tokenRecibo(),
                participacion.fechaHora(),
                "PROCESADO",
                true);
    }
}
