package com.universidad.votaciones.voting;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votaciones")
@CrossOrigin(origins = "*")
public class VotacionController {

    @PostMapping("/votar")
    public ResponseEntity<String> emitirVoto(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody VotoRequestDTO votoRequest) {
        
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body("La llave de idempotencia (X-Idempotency-Key) es obligatoria.");
        }

        return ResponseEntity.ok("Voto registrado con éxito para el estudiante: " + votoRequest.getIdEstudiante() 
            + " bajo la llave: " + idempotencyKey);
    }
}