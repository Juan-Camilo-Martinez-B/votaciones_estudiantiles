package com.universidad.votaciones.academicmock.service;

import com.universidad.votaciones.academicmock.model.EstudianteHabilitado;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;

import java.util.List;
import java.util.Optional;

public interface RegistroAcademicoService {

    RespuestaValidacionEstudiante validarEstudiante(String codigoEstudiante);

    Optional<EstudianteHabilitado> consultarEstudiante(String codigoEstudiante);

    long contarEstudiantesHabilitados();

    List<EstudianteHabilitado> listarEstudiantes();
}
