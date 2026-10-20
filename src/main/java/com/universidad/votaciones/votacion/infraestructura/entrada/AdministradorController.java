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
import org.springframework.web.bind.annotation.RestController;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarAdministradorUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarAdministradorUseCase;
import com.universidad.votaciones.votacion.dominio.Administrador;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth/administradores")
class AdministradorController {

    private final RegistrarAdministradorUseCase registrar;
    private final ConsultarAdministradorUseCase consultar;

    AdministradorController(RegistrarAdministradorUseCase registrar, ConsultarAdministradorUseCase consultar) {
        this.registrar = registrar;
        this.consultar = consultar;
    }

    record Solicitud(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Size(max = 100) String cargo) {
    }

    @PostMapping
    ResponseEntity<Administrador> crear(@Valid @RequestBody Solicitud solicitud) {
        Administrador administrador = registrar.registrar(new RegistrarAdministradorUseCase.Comando(
                solicitud.nombre(), solicitud.apellido(), solicitud.email(), solicitud.password(), solicitud.cargo()));
        return ResponseEntity.status(HttpStatus.CREATED).body(administrador);
    }

    @GetMapping("/{id}")
    Administrador buscarPorId(@PathVariable Long id) {
        return consultar.porId(id);
    }

    @GetMapping
    List<Administrador> listar() {
        return consultar.listar();
    }

    @GetMapping("/activos")
    List<Administrador> listarActivos() {
        return consultar.listarActivos();
    }

    @PatchMapping("/{id}/activar")
    Administrador activar(@PathVariable Long id) {
        return consultar.cambiarActivo(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    Administrador desactivar(@PathVariable Long id) {
        return consultar.cambiarActivo(id, false);
    }
}
