package com.universidad.votaciones.votacion.dominio;

public class EmailYaExisteException extends ReglaDeNegocioVioladaException {
    public EmailYaExisteException(String email) {
        super("El email '" + email + "' ya se encuentra registrado en el sistema");
    }
}
