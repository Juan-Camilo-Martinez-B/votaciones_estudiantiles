package com.universidad.votaciones.voting.internal.controller;

import com.universidad.votaciones.voting.VotacionApi;
import com.universidad.votaciones.voting.internal.dto.CandidatoDto;
import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador HTTP REST del Motor de Votación.
 * Se comunica exclusivamente a través de la fachada VotingApi.
 */
@RestController
@RequestMapping("/api/v1/votaciones")
public class VotacionController {

    private final VotacionApi votacionApi;

    public VotacionController(VotacionApi votacionApi) {
        this.votacionApi = votacionApi;
    }

    public record VotarHttpPayload(
            String llaveIdempotencia,
            String idEstudiante,
            String idCandidato) {
    }

    @PostMapping("/votar")
    public ResponseEntity<VotoResult> votar(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestBody VotarHttpPayload payload) {
        String llave = (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank())
                ? idempotencyKeyHeader.trim()
                : (payload.llaveIdempotencia() != null ? payload.llaveIdempotencia().trim() : null);

        EmitirVotoCommand command = new EmitirVotoCommand(
                llave,
                payload.idEstudiante(),
                payload.idCandidato());

        VotoResult resultado = votacionApi.emitirVoto(command);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/candidatos")
    public ResponseEntity<List<CandidatoDto>> listarCandidatos() {
        return ResponseEntity.ok(votacionApi.listarCandidatos());
    }

    @GetMapping("/resultados")
    public ResponseEntity<ConteoVotosDto> obtenerResultados() {
        return ResponseEntity.ok(votacionApi.obtenerResultadosUrna());
    }

    @GetMapping("/estudiante/{idEstudiante}/ha-votado")
    public ResponseEntity<Map<String, Object>> consultarSiHaVotado(@PathVariable String idEstudiante) {
        boolean yaVoto = votacionApi.haVotado(idEstudiante);
        return ResponseEntity.ok(Map.of(
                "idEstudiante", idEstudiante,
                "haVotado", yaVoto));
    }
}
