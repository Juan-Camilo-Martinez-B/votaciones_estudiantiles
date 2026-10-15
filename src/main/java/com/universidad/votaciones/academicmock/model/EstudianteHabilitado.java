package com.universidad.votaciones.academicmock.model;

public record EstudianteHabilitado(
        String codigoEstudiante,
        String nombreCompleto,
        String programaAcademico,
        int semestre,
        EstadoHabilitacion estado
) {
}
