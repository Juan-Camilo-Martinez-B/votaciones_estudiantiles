package com.universidad.votaciones.auth;

import com.universidad.votaciones.academicmock.AcademicApi;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import com.universidad.votaciones.audit.AuditApi;
import com.universidad.votaciones.audit.model.AccionAuditoria;
import com.universidad.votaciones.auth.controller.CrearUsuarioAdministradorRequest;
import com.universidad.votaciones.auth.controller.CrearVotanteEstudianteRequest;
import com.universidad.votaciones.auth.model.EstadoVotante;
import com.universidad.votaciones.auth.model.UsuarioAdministrador;
import com.universidad.votaciones.auth.model.VotanteEstudiante;
import com.universidad.votaciones.auth.service.UsuarioAdministradorService;
import com.universidad.votaciones.auth.service.VotanteEstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fachada pública oficial del Módulo de Autenticación y Gestión de Usuarios/Votantes.
 *
 * Expuesta en la raíz del paquete auth para coordinar la gestión de votantes
 * y administradores con otros módulos (como el Registro Académico y Auditoría),
 * preservando el encapsulamiento de servicios y repositorios internos.
 */
@Component
public class AuthApi {

    private final VotanteEstudianteService votanteService;
    private final UsuarioAdministradorService administradorService;

    @Autowired(required = false)
    private AcademicApi academicApi;

    @Autowired(required = false)
    private AuditApi auditApi;

    public AuthApi(VotanteEstudianteService votanteService,
                   UsuarioAdministradorService administradorService) {
        this.votanteService = votanteService;
        this.administradorService = administradorService;
    }

    /**
     * Da de alta un nuevo votante estudiante en el sistema (por defecto en estado PENDIENTE_VERIFICACION).
     */
    public VotanteEstudiante registrarVotante(CrearVotanteEstudianteRequest request) {
        VotanteEstudiante votante = votanteService.crear(request);
        if (auditApi != null) {
            auditApi.registrarExito(
                    request.codigoEstudiantil(),
                    AccionAuditoria.USUARIO_CREADO,
                    "Votante estudiante registrado: " + request.email(),
                    "AUTH"
            );
        }
        return votante;
    }

    /**
     * Registra un votante y, si el módulo AcademicApi está conectado y el estudiante
     * figura como habilitado en el censo oficial, lo activa automáticamente a estado HABILITADO.
     */
    public VotanteEstudiante registrarYHabilitarConRegistroAcademico(CrearVotanteEstudianteRequest request) {
        VotanteEstudiante votante = votanteService.crear(request);

        boolean habilitadoEnCenso = false;
        if (academicApi != null) {
            RespuestaValidacionEstudiante respuesta = academicApi.validarEstudiante(request.codigoEstudiantil());
            if (respuesta != null && respuesta.habilitado()) {
                habilitadoEnCenso = true;
            }
        }

        if (habilitadoEnCenso) {
            votante = votanteService.actualizarEstado(votante.getId(), EstadoVotante.HABILITADO);
        }

        if (auditApi != null) {
            auditApi.registrarExito(
                    request.codigoEstudiantil(),
                    AccionAuditoria.USUARIO_CREADO,
                    "Votante registrado y verificado contra censo académico (Estado: " + votante.getEstadoVotante() + ")",
                    "AUTH"
            );
        }

        return votante;
    }

    /**
     * Consulta si el votante identificado por código estudiantil existe y está en estado HABILITADO.
     */
    public boolean esVotanteHabilitado(String codigoEstudiantil) {
        return votanteService.esVotanteHabilitado(codigoEstudiantil);
    }

    /**
     * Busca un votante estudiante por su identificador único.
     */
    public VotanteEstudiante buscarVotantePorId(Long id) {
        return votanteService.buscarPorId(id);
    }

    /**
     * Busca un votante estudiante por su código estudiantil institucional.
     */
    public VotanteEstudiante buscarVotantePorCodigo(String codigoEstudiantil) {
        return votanteService.buscarPorCodigoEstudiantil(codigoEstudiantil);
    }

    public VotanteEstudiante buscarPorCodigoEstudiantil(String codigoEstudiantil) {
        return buscarVotantePorCodigo(codigoEstudiantil);
    }

    /**
     * Actualiza el estado de habilitación de un votante estudiante.
     */
    public VotanteEstudiante actualizarEstadoVotante(Long id, EstadoVotante nuevoEstado) {
        VotanteEstudiante actualizado = votanteService.actualizarEstado(id, nuevoEstado);
        if (auditApi != null) {
            auditApi.registrarExito(
                    actualizado.getCodigoEstudiantil(),
                    AccionAuditoria.USUARIO_ACTUALIZADO,
                    "Estado de votante actualizado a " + nuevoEstado,
                    "AUTH"
            );
        }
        return actualizado;
    }

    /**
     * Habilita a un votante estudiante por su código estudiantil.
     */
    public VotanteEstudiante habilitarVotantePorCodigo(String codigoEstudiantil) {
        VotanteEstudiante votante = votanteService.buscarPorCodigoEstudiantil(codigoEstudiantil);
        return actualizarEstadoVotante(votante.getId(), EstadoVotante.HABILITADO);
    }

    /**
     * Lista todos los votantes registrados.
     */
    public List<VotanteEstudiante> listarVotantes() {
        return votanteService.listarTodos();
    }

    /**
     * Da de alta un nuevo administrador en el sistema.
     */
    public UsuarioAdministrador registrarAdministrador(CrearUsuarioAdministradorRequest request) {
        UsuarioAdministrador admin = administradorService.crear(request);
        if (auditApi != null) {
            auditApi.registrarExito(
                    admin.getEmail(),
                    AccionAuditoria.USUARIO_CREADO,
                    "Usuario administrador creado: " + admin.getEmail(),
                    "AUTH"
            );
        }
        return admin;
    }

    /**
     * Busca un administrador por su ID.
     */
    public UsuarioAdministrador buscarAdministradorPorId(Long id) {
        return administradorService.buscarPorId(id);
    }

    /**
     * Lista todos los administradores registrados.
     */
    public List<UsuarioAdministrador> listarAdministradores() {
        return administradorService.listarTodos();
    }
}
