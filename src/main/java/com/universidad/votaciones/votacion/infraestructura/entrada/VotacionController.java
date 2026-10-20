package com.universidad.votaciones.votacion.infraestructura.entrada;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CargarPadronUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CerrarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarCandidatosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarParticipacionUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarResultadosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.EmitirVotoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.IniciarJornadaUseCase;
import com.universidad.votaciones.votacion.dominio.Candidato;
import com.universidad.votaciones.votacion.dominio.Escrutinio;
import com.universidad.votaciones.votacion.dominio.EstadoDeJornada;
import com.universidad.votaciones.votacion.dominio.ReciboDeVoto;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;
import com.universidad.votaciones.votacion.dominio.VotanteYaVotoException;

@RestController
@RequestMapping("/api/v1/votaciones")
class VotacionController {

    private final IniciarJornadaUseCase iniciarJornada;
    private final CerrarJornadaUseCase cerrarJornada;
    private final CargarPadronUseCase cargarPadron;
    private final EmitirVotoUseCase emitirVoto;
    private final ConsultarJornadaUseCase consultarJornada;
    private final ConsultarCandidatosUseCase consultarCandidatos;
    private final ConsultarResultadosUseCase consultarResultados;
    private final ConsultarParticipacionUseCase consultarParticipacion;

    VotacionController(IniciarJornadaUseCase iniciarJornada,
                       CerrarJornadaUseCase cerrarJornada,
                       CargarPadronUseCase cargarPadron,
                       EmitirVotoUseCase emitirVoto,
                       ConsultarJornadaUseCase consultarJornada,
                       ConsultarCandidatosUseCase consultarCandidatos,
                       ConsultarResultadosUseCase consultarResultados,
                       ConsultarParticipacionUseCase consultarParticipacion) {
        this.iniciarJornada = iniciarJornada;
        this.cerrarJornada = cerrarJornada;
        this.cargarPadron = cargarPadron;
        this.emitirVoto = emitirVoto;
        this.consultarJornada = consultarJornada;
        this.consultarCandidatos = consultarCandidatos;
        this.consultarResultados = consultarResultados;
        this.consultarParticipacion = consultarParticipacion;
    }

    @PostMapping("/jornada/iniciar")
    EstadoDeJornada iniciar() {
        return iniciarJornada.iniciar();
    }

    @PostMapping("/jornada/cerrar")
    EstadoDeJornada cerrar() {
        return cerrarJornada.cerrar();
    }

    @GetMapping("/jornada")
    EstadoDeJornada jornada() {
        return consultarJornada.consultar();
    }

    @PostMapping("/padron/cargar")
    Map<String, Integer> cargarPadron() {
        return Map.of("estudiantesCargados", cargarPadron.cargar());
    }

    record VotarHttpPayload(String llaveIdempotencia, String idEstudiante, String idCandidato) {
    }

    @PostMapping("/votar")
    ReciboDeVoto votar(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader,
            @RequestBody VotarHttpPayload payload) {
        String llave = (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank())
                ? idempotencyKeyHeader.trim()
                : payload.llaveIdempotencia();
        return emitirVoto.emitir(new EmitirVotoUseCase.Comando(llave, payload.idEstudiante(), payload.idCandidato()));
    }

    @GetMapping("/candidatos")
    List<Candidato> candidatos() {
        return consultarCandidatos.listar();
    }

    @GetMapping("/resultados")
    Escrutinio resultados() {
        return consultarResultados.consultarResultados();
    }

    @GetMapping("/estudiante/{idEstudiante}/ha-votado")
    Map<String, Object> haVotado(@PathVariable String idEstudiante) {
        return Map.of(
                "idEstudiante", idEstudiante,
                "haVotado", consultarParticipacion.haVotado(idEstudiante));
    }

    @ExceptionHandler(VotanteYaVotoException.class)
    ResponseEntity<Map<String, String>> yaVoto(VotanteYaVotoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ReglaDeNegocioVioladaException.class)
    ResponseEntity<Map<String, String>> reglaViolada(ReglaDeNegocioVioladaException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of("error", e.getMessage()));
    }
}
