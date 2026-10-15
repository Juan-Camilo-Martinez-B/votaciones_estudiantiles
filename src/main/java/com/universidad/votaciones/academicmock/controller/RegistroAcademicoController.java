package com.universidad.votaciones.academicmock.controller;

import com.universidad.votaciones.academicmock.model.EstudianteHabilitado;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import com.universidad.votaciones.academicmock.service.RegistroAcademicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/academic")
public class RegistroAcademicoController {

    private final RegistroAcademicoService registroAcademicoService;

    public RegistroAcademicoController(RegistroAcademicoService registroAcademicoService) {
        this.registroAcademicoService = registroAcademicoService;
    }

    @GetMapping("/estudiantes/{codigo}/validar")
    public ResponseEntity<RespuestaValidacionEstudiante> validarEstudiante(@PathVariable String codigo) {
        RespuestaValidacionEstudiante respuesta = registroAcademicoService.validarEstudiante(codigo);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/estudiantes/{codigo}")
    public ResponseEntity<EstudianteHabilitado> consultarEstudiante(@PathVariable String codigo) {
        return registroAcademicoService.consultarEstudiante(codigo)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/estudiantes/cantidad")
    public ResponseEntity<Map<String, Long>> cantidadEstudiantesHabilitados() {
        long cantidad = registroAcademicoService.contarEstudiantesHabilitados();
        return ResponseEntity.ok(Map.of("estudiantesHabilitados", cantidad));
    }
}
