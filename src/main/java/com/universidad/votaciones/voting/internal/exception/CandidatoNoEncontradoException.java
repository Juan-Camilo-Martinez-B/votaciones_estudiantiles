package com.universidad.votaciones.voting.internal.exception;

public class CandidatoNoEncontradoException extends VotacionException {
    public CandidatoNoEncontradoException(String idCandidato) {
        super("El candidato con identificador '" + idCandidato + "' no existe en el sistema electoral.");
    }
}
