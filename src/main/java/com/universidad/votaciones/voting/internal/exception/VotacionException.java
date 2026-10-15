package com.universidad.votaciones.voting.internal.exception;

public class VotacionException extends RuntimeException {
    public VotacionException(String message) {
        super(message);
    }

    public VotacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
