package com.universidad.votaciones.votacion.infraestructura.entrada;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CambiarEstadoVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarVotanteUseCase;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth/votantes")
class VotanteController {

    private final RegistrarVotanteUseCase registrar;
    private final ConsultarVotanteUseCase consultar;
    private final CambiarEstadoVotanteUseCase cambiarEstado;

    VotanteController(RegistrarVotanteUseCase registrar, ConsultarVotanteUseCase consultar,
                      CambiarEstadoVotanteUseCase cambiarEstado) {
        this.registrar = registrar;
        this.consultar = consultar;
        this.cambiarEstado = cambiarEstado;
    }

    record Solicitud(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Size(max = 50) String codigoEstudiantil,
            @NotBlank @Size(max = 150) String programaAcademico) {
    }

    @PostMapping
    ResponseEntity<Votante> crear(@Valid @RequestBody Solicitud solicitud) {
        Votante votante = registrar.registrar(new RegistrarVotanteUseCase.Comando(
                solicitud.nombre(), solicitud.apellido(), solicitud.email(), solicitud.password(),
                solicitud.codigoEstudiantil(), solicitud.programaAcademico()));
        return ResponseEntity.status(HttpStatus.CREATED).body(votante);
    }

    @GetMapping("/{id}")
    Votante buscarPorId(@PathVariable Long id) {
        return consultar.porId(id);
    }

    @GetMapping("/codigo/{codigoEstudiantil}")
    Votante buscarPorCodigo(@PathVariable String codigoEstudiantil) {
        return consultar.porCodigo(codigoEstudiantil);
    }

    @GetMapping
    List<Votante> listar(@RequestParam(required = false) EstadoVotante estado) {
        return consultar.listar(estado);
    }

    @GetMapping("/habilitado/{codigoEstudiantil}")
    boolean estaHabilitado(@PathVariable String codigoEstudiantil) {
        return consultar.estaHabilitado(codigoEstudiantil);
    }

    @PatchMapping("/{id}/estado")
    Votante actualizarEstado(@PathVariable Long id, @RequestParam EstadoVotante estado) {
        return cambiarEstado.cambiar(id, estado);
    }
}
