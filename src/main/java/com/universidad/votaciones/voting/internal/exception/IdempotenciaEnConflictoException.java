package com.universidad.votaciones.voting.internal.exception;

public class IdempotenciaEnConflictoException extends VotacionException {
    public IdempotenciaEnConflictoException(String mensaje) {
        super(mensaje);
    }
}
