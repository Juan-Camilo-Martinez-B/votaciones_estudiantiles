package com.universidad.votaciones.auth.service;

public class EmailYaExisteException extends RuntimeException {

    public EmailYaExisteException(String email) {
        super("El email '" + email + "' ya se encuentra registrado en el sistema");
    }
}
