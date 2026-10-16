package com.universidad.votaciones.voting;

import com.universidad.votaciones.academicmock.AcademicApi;
import com.universidad.votaciones.audit.AuditApi;
import com.universidad.votaciones.audit.model.AccionAuditoria;
import com.universidad.votaciones.auth.AuthApi;
import com.universidad.votaciones.voting.internal.dto.CandidatoDto;
import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import com.universidad.votaciones.voting.internal.exception.CandidatoNoEncontradoException;
import com.universidad.votaciones.voting.internal.exception.VotacionException;
import com.universidad.votaciones.voting.internal.exception.VotanteYaVotoException;
import com.universidad.votaciones.voting.internal.service.VotacionInternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fachada pública oficial del Módulo 2: Motor de Votación e Idempotencia (Core Transaccional).
 * Diseñado y desarrollado para: Juan Camilo Martínez.
 *
 * Es la ÚNICA clase expuesta en la raíz del paquete voting para interactuar con
 * otros módulos.
 * Todo el core de datos, repositorios, controladores y servicios reside
 * encapsulado en la capa internal.
 *
 * Conecta externamente con las APIs públicas de:
 * - AuthApi (para verificación de votantes habilitados)
 * - AcademicApi (para comprobación con el censo académico oficial)
 * - AuditApi (para trazabilidad transaccional asíncrona)
 */
@Component
public class VotacionApi {

    private final VotacionInternalService votacionInternalService;

    @Autowired(required = false)
    private AuthApi authApi;

    @Autowired(required = false)
    private AcademicApi academicApi;

    @Autowired(required = false)
    private AuditApi auditApi;

    @Autowired
    public VotacionApi(VotacionInternalService votacionInternalService,
                       @Autowired(required = false) AuthApi authApi,
                       @Autowired(required = false) AcademicApi academicApi,
                       @Autowired(required = false) AuditApi auditApi) {
        this.votacionInternalService = votacionInternalService;
        this.authApi = authApi;
        this.academicApi = academicApi;
        this.auditApi = auditApi;
    }

    public VotacionApi(VotacionInternalService votacionInternalService) {
        this(votacionInternalService, null, null, null);
    }

    /**
     * Procesa la emisión de un voto aplicando verificación intermodular,
     * llaves de idempotencia y depósito anónimo en la urna digital.
     *
     * @param command Comando con la llave de idempotencia, identificador del
     *                estudiante y candidato.
     * @return Resultado del voto (procesado o respuesta idempotente).
     */
    public VotoResult emitirVoto(EmitirVotoCommand command) {
        // 1. Verificación intermodular de habilitación (si AuthApi o AcademicApi están disponibles)
        validarHabilitacionEstudiante(command.idEstudiante());

        // 2. Procesamiento transaccional en el core interno
        try {
            VotoResult resultado = votacionInternalService.procesarVoto(command);

            // 3. Notificación de auditoría transaccional vía AuditApi
            if (auditApi != null) {
                if (resultado.esIdempotente()) {
                    auditApi.registrarExito(
                            command.idEstudiante(),
                            AccionAuditoria.VOTO_REGISTRADO,
                            "Voto idempotente ya procesado reconocido para llave: " + command.llaveIdempotencia(),
                            "VOTING"
                    );
                } else {
                    auditApi.registrarExito(
                            command.idEstudiante(),
                            AccionAuditoria.VOTO_REGISTRADO,
                            "Voto depositado en urna anónima. Llave: " + command.llaveIdempotencia() + ", Token: " + resultado.tokenAnonimo(),
                            "VOTING"
                    );
                }
            }

            return resultado;

        } catch (VotanteYaVotoException e) {
            if (auditApi != null) {
                auditApi.registrarFallo(
                        command.idEstudiante(),
                        AccionAuditoria.VOTO_DUPLICADO_RECHAZADO,
                        "Intento de voto duplicado rechazado: " + e.getMessage(),
                        "VOTING"
                );
            }
            throw e;
        } catch (CandidatoNoEncontradoException e) {
            if (auditApi != null) {
                auditApi.registrarFallo(
                        command.idEstudiante(),
                        AccionAuditoria.VOTO_RECHAZADO,
                        "Candidato no encontrado en tarjetón: " + e.getMessage(),
                        "VOTING"
                );
            }
            throw e;
        } catch (Exception e) {
            if (auditApi != null) {
                auditApi.registrarFallo(
                        command.idEstudiante(),
                        AccionAuditoria.VOTO_RECHAZADO,
                        "Error al emitir voto: " + e.getMessage(),
                        "VOTING"
                );
            }
            throw e;
        }
    }

    /**
     * Valida si el estudiante está habilitado consultando primero AuthApi y/o AcademicApi.
     */
    private void validarHabilitacionEstudiante(String idEstudiante) {
        if (idEstudiante == null || idEstudiante.isBlank()) {
            throw new VotacionException("El identificador del estudiante es obligatorio.");
        }

        // Si authApi está conectado, comprobar si el votante está registrado y habilitado
        if (authApi != null) {
            boolean habilitadoEnAuth = authApi.esVotanteHabilitado(idEstudiante);
            if (habilitadoEnAuth) {
                return; // Estudiante habilitado en el padrón electoral de auth
            }

            // Si no está habilitado en auth, verificar si existe en el censo oficial de AcademicApi
            if (academicApi != null) {
                boolean habilitadoEnAcademic = academicApi.esEstudianteHabilitado(idEstudiante);
                if (habilitadoEnAcademic) {
                    return; // Estudiante habilitado según registro académico oficial
                }
            }

            // Si está registrado en auth o en academic pero no habilitado, rechazar
            if (auditApi != null) {
                auditApi.registrarFallo(
                        idEstudiante,
                        AccionAuditoria.VOTO_RECHAZADO,
                        "Estudiante no habilitado para votar: " + idEstudiante,
                        "VOTING"
                );
            }
            throw new VotacionException("El estudiante '" + idEstudiante + "' no se encuentra habilitado para votar.");
        }

        // Si solo academicApi está disponible
        if (academicApi != null) {
            boolean habilitadoEnAcademic = academicApi.esEstudianteHabilitado(idEstudiante);
            if (!habilitadoEnAcademic) {
                if (auditApi != null) {
                    auditApi.registrarFallo(
                            idEstudiante,
                            AccionAuditoria.VOTO_RECHAZADO,
                            "Estudiante no encontrado o inhabilitado en registro académico: " + idEstudiante,
                            "VOTING"
                    );
                }
                throw new VotacionException("El estudiante '" + idEstudiante + "' no se encuentra habilitado en el registro académico.");
            }
        }
    }

    /**
     * Consulta si un estudiante ya ejerció su voto en este proceso electoral.
     */
    public boolean haVotado(String idEstudiante) {
        return votacionInternalService.haVotado(idEstudiante);
    }

    /**
     * Lista los candidatos registrados en el tarjetón electoral.
     */
    public List<CandidatoDto> listarCandidatos() {
        return votacionInternalService.listarCandidatos();
    }

    /**
     * Registra o da de alta un candidato para la elección.
     */
    public CandidatoDto registrarCandidato(String idCandidato, String numeroTarjeton, String nombreCandidato,
            String propuestaResumen) {
        CandidatoDto registrado = votacionInternalService.registrarCandidato(
                new CandidatoDto(idCandidato, numeroTarjeton, nombreCandidato, propuestaResumen));

        if (auditApi != null) {
            auditApi.registrarExito(
                    "SISTEMA",
                    AccionAuditoria.CANDIDATO_REGISTRADO,
                    "Candidato registrado: " + nombreCandidato + " (#" + numeroTarjeton + ")",
                    "VOTING"
            );
        }

        return registrado;
    }

    /**
     * Obtiene el conteo total y escrutinio agrupado de la urna anónima.
     */
    public ConteoVotosDto obtenerResultadosUrna() {
        ConteoVotosDto resultados = votacionInternalService.obtenerResultadosUrna();

        if (auditApi != null) {
            auditApi.registrarExito(
                    "SISTEMA",
                    AccionAuditoria.RESULTADOS_CONSULTADOS,
                    "Consulta de escrutinio general de la urna anónima. Total votos: " + resultados.totalVotos(),
                    "VOTING"
            );
        }

        return resultados;
    }
}
