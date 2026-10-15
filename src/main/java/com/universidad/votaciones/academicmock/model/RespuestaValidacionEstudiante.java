package com.universidad.votaciones.academicmock.model;

import java.time.Instant;

public record RespuestaValidacionEstudiante(
        String codigoEstudiante,
        boolean habilitado,
        EstadoHabilitacion estado,
        String mensaje,
        Instant timestamp
) {

    public static RespuestaValidacionEstudiante habilitado(String codigoEstudiante, EstadoHabilitacion estado) {
        return new RespuestaValidacionEstudiante(
                codigoEstudiante,
                true,
                estado,
                "Estudiante encontrado y habilitado para votar",
                Instant.now()
        );
    }

    public static RespuestaValidacionEstudiante noHabilitado(String codigoEstudiante, String mensaje) {
        return new RespuestaValidacionEstudiante(
                codigoEstudiante,
                false,
                EstadoHabilitacion.NO_HABILITADO,
                mensaje,
                Instant.now()
        );
    }
}
