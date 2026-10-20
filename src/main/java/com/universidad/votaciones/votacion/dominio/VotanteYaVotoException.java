package com.universidad.votaciones.votacion.dominio;

public class VotanteYaVotoException extends ReglaDeNegocioVioladaException {
    public VotanteYaVotoException(String idEstudiante) {
        super("El estudiante con identificador '" + idEstudiante
                + "' ya ha ejercido su derecho al voto.");
    }
}
