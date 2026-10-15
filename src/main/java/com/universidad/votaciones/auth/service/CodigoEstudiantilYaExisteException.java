package com.universidad.votaciones.auth.service;

public class CodigoEstudiantilYaExisteException extends RuntimeException {

    public CodigoEstudiantilYaExisteException(String codigoEstudiantil) {
        super("El código estudiantil '" + codigoEstudiantil + "' ya se encuentra registrado");
    }
}
