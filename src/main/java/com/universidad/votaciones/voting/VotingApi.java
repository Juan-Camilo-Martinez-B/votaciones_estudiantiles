package com.universidad.votaciones.voting;

import com.universidad.votaciones.voting.internal.dto.CandidatoDto;
import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import com.universidad.votaciones.voting.internal.service.VotacionInternalService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fachada pública oficial del Módulo 2: Motor de Votación e Idempotencia (Core Transaccional).
 * Diseñado y desarrollado para: Juan Camilo Martínez.
 *
 * Es la ÚNICA clase expuesta en la raíz del paquete voting para interactuar con otros módulos.
 * Todo el core de datos, repositorios, controladores y servicios reside encapsulado en la capa internal.
 */
@Component
public class VotingApi {

    private final VotacionInternalService votacionInternalService;

    public VotingApi(VotacionInternalService votacionInternalService) {
        this.votacionInternalService = votacionInternalService;
    }

    /**
     * Procesa la emisión de un voto aplicando llaves de idempotencia y depósito
     * anónimo en la urna digital.
     *
     * @param command Comando con la llave de idempotencia, identificador del estudiante y candidato.
     * @return Resultado del voto (procesado o respuesta idempotente).
     */
    public VotoResult emitirVoto(EmitirVotoCommand command) {
        return votacionInternalService.procesarVoto(command);
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
    public CandidatoDto registrarCandidato(String idCandidato, String numeroTarjeton, String nombreCandidato, String propuestaResumen) {
        return votacionInternalService.registrarCandidato(
                new CandidatoDto(idCandidato, numeroTarjeton, nombreCandidato, propuestaResumen)
        );
    }

    /**
     * Obtiene el conteo total y escrutinio agrupado de la urna anónima.
     */
    public ConteoVotosDto obtenerResultadosUrna() {
        return votacionInternalService.obtenerResultadosUrna();
    }
}
