package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Papeleta de la urna. No tiene estudiante, llave de idempotencia ni recibo:
 * esos datos viven solo en {@link Participacion}.
 */
public final class Papeleta {

    private final String idPapeleta;
    private final String idCandidato;
    private final LocalDateTime fechaHoraRegistro;

    private Papeleta(String idPapeleta, String idCandidato, LocalDateTime fechaHoraRegistro) {
        this.idPapeleta = idPapeleta;
        this.idCandidato = idCandidato;
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    static Papeleta anonima(String idCandidato, LocalDateTime momento) {
        return new Papeleta(UUID.randomUUID().toString(), idCandidato, momento);
    }

    public static Papeleta reconstruir(String idPapeleta, String idCandidato, LocalDateTime fechaHoraRegistro) {
        return new Papeleta(idPapeleta, idCandidato, fechaHoraRegistro);
    }

    public String idPapeleta() {
        return idPapeleta;
    }

    public String idCandidato() {
        return idCandidato;
    }

    public LocalDateTime fechaHoraRegistro() {
        return fechaHoraRegistro;
    }
}
