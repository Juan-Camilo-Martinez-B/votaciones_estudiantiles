package com.universidad.votaciones.votacion.dominio;

public class CodigoEstudiantilYaExisteException extends ReglaDeNegocioVioladaException {
    public CodigoEstudiantilYaExisteException(String codigoEstudiantil) {
        super("El código estudiantil '" + codigoEstudiantil + "' ya se encuentra registrado");
    }
}
