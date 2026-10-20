package com.universidad.votaciones.votacion.dominio;

public class ReglaDeNegocioVioladaException extends RuntimeException {
    public ReglaDeNegocioVioladaException(String mensaje) {
        super(mensaje);
    }
}
