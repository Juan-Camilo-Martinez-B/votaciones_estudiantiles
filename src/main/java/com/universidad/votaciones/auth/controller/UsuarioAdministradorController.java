package com.universidad.votaciones.auth.controller;

import com.universidad.votaciones.auth.model.UsuarioAdministrador;
import com.universidad.votaciones.auth.service.UsuarioAdministradorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/administradores")
public class UsuarioAdministradorController {

    private final UsuarioAdministradorService administradorService;

    public UsuarioAdministradorController(UsuarioAdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @PostMapping
    public ResponseEntity<UsuarioAdministrador> crear(@Valid @RequestBody CrearUsuarioAdministradorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administradorService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioAdministrador> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioAdministrador>> listarTodos() {
        return ResponseEntity.ok(administradorService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioAdministrador>> listarActivos() {
        return ResponseEntity.ok(administradorService.listarActivos());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioAdministrador> activar(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioAdministrador> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.desactivar(id));
    }
}
