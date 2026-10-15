package com.universidad.votaciones.voting.internal.service;

import com.universidad.votaciones.voting.internal.dto.CandidatoDto;
import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import com.universidad.votaciones.voting.internal.exception.CandidatoNoEncontradoException;
import com.universidad.votaciones.voting.internal.exception.IdempotenciaEnConflictoException;
import com.universidad.votaciones.voting.internal.exception.VotanteYaVotoException;
import com.universidad.votaciones.voting.internal.model.Candidato;
import com.universidad.votaciones.voting.internal.model.EstadoProcesamiento;
import com.universidad.votaciones.voting.internal.model.PapeletaVoto;
import com.universidad.votaciones.voting.internal.model.TransaccionVotoIdempotente;
import com.universidad.votaciones.voting.internal.repository.CandidatoRepository;
import com.universidad.votaciones.voting.internal.repository.PapeletaVotoRepository;
import com.universidad.votaciones.voting.internal.repository.TransaccionVotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio central del Módulo 2: Motor de Votación e Idempotencia (Core Transaccional).
 * Responsable de la lógica transaccional de votación, control estricto de llaves de idempotencia
 * para evitar votos duplicados y depósito en la urna anónima sin trazabilidad hacia el votante.
 */
@Service
public class VotacionInternalService {

    private static final Logger log = LoggerFactory.getLogger(VotacionInternalService.class);

    private final TransaccionVotoRepository transaccionVotoRepository;
    private final PapeletaVotoRepository papeletaVotoRepository;
    private final CandidatoRepository candidatoRepository;

    public VotacionInternalService(TransaccionVotoRepository transaccionVotoRepository,
                                   PapeletaVotoRepository papeletaVotoRepository,
                                   CandidatoRepository candidatoRepository) {
        this.transaccionVotoRepository = transaccionVotoRepository;
        this.papeletaVotoRepository = papeletaVotoRepository;
        this.candidatoRepository = candidatoRepository;
    }

    @Transactional
    public VotoResult procesarVoto(EmitirVotoCommand command) {
        // 1. Verificación de Idempotencia: ¿La llave de idempotencia ya fue procesada?
        Optional<TransaccionVotoIdempotente> txExistente = transaccionVotoRepository.findByLlaveIdempotencia(command.llaveIdempotencia());
        if (txExistente.isPresent()) {
            TransaccionVotoIdempotente tx = txExistente.get();
            if (tx.getEstado() == EstadoProcesamiento.PROCESADO) {
                log.info("Petición idempotente detectada para llave: {}. Retornando resultado previo sin duplicar voto en la urna.",
                        command.llaveIdempotencia());
                return new VotoResult(
                        "Voto previamente procesado (Respuesta Idempotente)",
                        tx.getLlaveIdempotencia(),
                        tx.getTokenAnonimo(),
                        tx.getFechaHoraPeticion(),
                        tx.getEstado().name(),
                        true
                );
            } else if (tx.getEstado() == EstadoProcesamiento.PENDIENTE) {
                throw new IdempotenciaEnConflictoException("Transacción en curso para la llave de idempotencia: " + command.llaveIdempotencia());
            }
        }

        // 2. Control de Voto Único por Estudiante: ¿El estudiante ya votó con otra llave?
        if (transaccionVotoRepository.existsByIdEstudianteAndEstado(command.idEstudiante(), EstadoProcesamiento.PROCESADO)) {
            log.warn("Intento de doble voto rechazado para el estudiante: {}", command.idEstudiante());
            throw new VotanteYaVotoException(command.idEstudiante());
        }

        // 3. Validar existencia del candidato seleccionado en el tarjetón
        Candidato candidato = candidatoRepository.findById(command.idCandidato())
                .orElseThrow(() -> new CandidatoNoEncontradoException(command.idCandidato()));

        // 4. Procesamiento atómico del voto y depósito en urna anónima
        String tokenAnonimo = UUID.randomUUID().toString();
        LocalDateTime momento = LocalDateTime.now();

        try {
            // A) Registrar la transacción idempotente vinculada al estudiante
            TransaccionVotoIdempotente transaccion = new TransaccionVotoIdempotente(
                    command.llaveIdempotencia(),
                    command.idEstudiante(),
                    tokenAnonimo,
                    momento,
                    EstadoProcesamiento.PROCESADO
            );
            transaccionVotoRepository.save(transaccion);

            // B) Depositar la papeleta anónima en la urna digital (CERO vínculo con el idEstudiante)
            String idPapeleta = UUID.randomUUID().toString();
            PapeletaVoto papeleta = new PapeletaVoto(
                    idPapeleta,
                    candidato.getIdCandidato(),
                    tokenAnonimo,
                    momento
            );
            papeletaVotoRepository.save(papeleta);

            log.info("Voto emitido exitosamente. Llave: {}, Token: {}", command.llaveIdempotencia(), tokenAnonimo);

            return new VotoResult(
                    "Voto registrado exitosamente.",
                    command.llaveIdempotencia(),
                    tokenAnonimo,
                    momento,
                    EstadoProcesamiento.PROCESADO.name(),
                    false
            );

        } catch (DataIntegrityViolationException e) {
            // Manejo de condición de carrera concurrente con la misma llave
            log.warn("Violación de integridad detectada para llave {}. Verificando idempotencia concurrente.", command.llaveIdempotencia());
            Optional<TransaccionVotoIdempotente> reintento = transaccionVotoRepository.findByLlaveIdempotencia(command.llaveIdempotencia());
            if (reintento.isPresent() && reintento.get().getEstado() == EstadoProcesamiento.PROCESADO) {
                TransaccionVotoIdempotente tx = reintento.get();
                return new VotoResult(
                        "Voto previamente procesado (Respuesta Idempotente)",
                        tx.getLlaveIdempotencia(),
                        tx.getTokenAnonimo(),
                        tx.getFechaHoraPeticion(),
                        tx.getEstado().name(),
                        true
                );
            }
            throw e;
        }
    }

    public boolean haVotado(String idEstudiante) {
        return transaccionVotoRepository.existsByIdEstudianteAndEstado(idEstudiante, EstadoProcesamiento.PROCESADO);
    }

    public List<CandidatoDto> listarCandidatos() {
        return candidatoRepository.findAll().stream()
                .map(c -> new CandidatoDto(c.getIdCandidato(), c.getNumeroTarjeton(), c.getNombreCandidato(), c.getPropuestaResumen()))
                .toList();
    }

    @Transactional
    public CandidatoDto registrarCandidato(CandidatoDto dto) {
        Candidato entidad = new Candidato(dto.idCandidato(), dto.numeroTarjeton(), dto.nombreCandidato(), dto.propuestaResumen());
        Candidato guardado = candidatoRepository.save(entidad);
        return new CandidatoDto(guardado.getIdCandidato(), guardado.getNumeroTarjeton(), guardado.getNombreCandidato(), guardado.getPropuestaResumen());
    }

    public ConteoVotosDto obtenerResultadosUrna() {
        long totalVotos = papeletaVotoRepository.count();
        List<Object[]> conteos = papeletaVotoRepository.contarVotosPorCandidato();
        Map<String, Long> mapa = new HashMap<>();
        for (Object[] fila : conteos) {
            String idCandidato = (String) fila[0];
            Long total = (Long) fila[1];
            mapa.put(idCandidato, total);
        }
        return new ConteoVotosDto(totalVotos, mapa);
    }
}
