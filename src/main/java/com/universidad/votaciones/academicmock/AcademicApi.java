package com.universidad.votaciones.academicmock;

import com.universidad.votaciones.academicmock.model.EstadoHabilitacion;
import com.universidad.votaciones.academicmock.model.EstudianteHabilitado;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import com.universidad.votaciones.academicmock.service.RegistroAcademicoService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Fachada pública oficial del Módulo 1: Registro Académico (Censo Estudiantil Oficial).
 *
 * Expuesta en la raíz del paquete academicmock para permitir la comunicación
 * de otros módulos exclusivamente a través de esta API, preservando el
 * encapsulamiento de controladores y servicios internos.
 */
@Component
public class AcademicApi {

    private final RegistroAcademicoService registroAcademicoService;

    public AcademicApi(RegistroAcademicoService registroAcademicoService) {
        this.registroAcademicoService = registroAcademicoService;
    }

    /**
     * Valida si un estudiante existe en el censo oficial y si se encuentra habilitado para votar.
     */
    public RespuestaValidacionEstudiante validarEstudiante(String codigoEstudiante) {
        return registroAcademicoService.validarEstudiante(codigoEstudiante);
    }

    /**
     * Consulta el detalle del estudiante habilitado en el registro académico.
     */
    public Optional<EstudianteHabilitado> consultarEstudiante(String codigoEstudiante) {
        return registroAcademicoService.consultarEstudiante(codigoEstudiante);
    }

    /**
     * Indica directamente si el estudiante existe y su estado es HABILITADO.
     */
    public boolean esEstudianteHabilitado(String codigoEstudiante) {
        RespuestaValidacionEstudiante respuesta = registroAcademicoService.validarEstudiante(codigoEstudiante);
        return respuesta != null && respuesta.habilitado() && respuesta.estado() == EstadoHabilitacion.HABILITADO;
    }

    /**
     * Retorna la cantidad total de estudiantes registrados en el censo académico.
     */
    public long contarEstudiantesHabilitados() {
        return registroAcademicoService.contarEstudiantesHabilitados();
    }

    /**
     * Lista los estudiantes habilitados del censo simulado para cargarlos al padrón electoral.
     */
    public List<EstudianteHabilitado> listarEstudiantesHabilitados() {
        return registroAcademicoService.listarEstudiantes().stream()
                .filter(estudiante -> estudiante.estado() == EstadoHabilitacion.HABILITADO)
                .toList();
    }
}
