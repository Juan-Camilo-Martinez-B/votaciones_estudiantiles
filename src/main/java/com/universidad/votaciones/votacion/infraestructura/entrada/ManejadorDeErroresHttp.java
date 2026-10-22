package com.universidad.votaciones.votacion.infraestructura.entrada;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ManejadorDeErroresHttp {

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Map<String, Object>> runtime(RuntimeException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("error", "Error de Validación en el Sistema de Votación");
        cuerpo.put("message", ex.getMessage());
        return new ResponseEntity<>(cuerpo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> general(Exception ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        cuerpo.put("error", "Error Interno del Servidor");
        cuerpo.put("message", ex.getMessage());
        return new ResponseEntity<>(cuerpo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
