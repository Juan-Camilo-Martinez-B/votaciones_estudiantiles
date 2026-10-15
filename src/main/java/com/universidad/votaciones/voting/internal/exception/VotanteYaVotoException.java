package com.universidad.votaciones.voting.internal.exception;

public class VotanteYaVotoException extends VotacionException {
    public VotanteYaVotoException(String idEstudiante) {
        super("El estudiante con identificador '" + idEstudiante + "' ya ha ejercido su derecho al voto previamente.");
    }
}
