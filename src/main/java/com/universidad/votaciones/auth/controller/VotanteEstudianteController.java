package com.universidad.votaciones.auth.controller;

import com.universidad.votaciones.auth.model.EstadoVotante;
import com.universidad.votaciones.auth.model.VotanteEstudiante;
import com.universidad.votaciones.auth.service.VotanteEstudianteService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/auth/votantes")
public class VotanteEstudianteController {

    private final VotanteEstudianteService votanteService;

    public VotanteEstudianteController(VotanteEstudianteService votanteService) {
        this.votanteService = votanteService;
    }

    @PostMapping
    public ResponseEntity<VotanteEstudiante> crear(@Valid @RequestBody CrearVotanteEstudianteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(votanteService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VotanteEstudiante> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(votanteService.buscarPorId(id));
    }

    @GetMapping("/codigo/{codigoEstudiantil}")
    public ResponseEntity<VotanteEstudiante> buscarPorCodigoEstudiantil(@PathVariable String codigoEstudiantil) {
        return ResponseEntity.ok(votanteService.buscarPorCodigoEstudiantil(codigoEstudiantil));
    }

    @GetMapping
    public ResponseEntity<List<VotanteEstudiante>> listarTodos(
            @RequestParam(required = false) EstadoVotante estado) {
        if (estado != null) {
            return ResponseEntity.ok(votanteService.listarPorEstado(estado));
        }
        return ResponseEntity.ok(votanteService.listarTodos());
    }

    @GetMapping("/habilitado/{codigoEstudiantil}")
    public ResponseEntity<Boolean> esVotanteHabilitado(@PathVariable String codigoEstudiantil) {
        return ResponseEntity.ok(votanteService.esVotanteHabilitado(codigoEstudiantil));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<VotanteEstudiante> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoVotante estado) {
        return ResponseEntity.ok(votanteService.actualizarEstado(id, estado));
    }
}
