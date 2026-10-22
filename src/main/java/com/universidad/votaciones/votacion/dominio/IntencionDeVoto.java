package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDateTime;

/**
 * Intención todavía identificada. El único camino hacia la urna es {@link #privatizar},
 * que descarta al estudiante y deja solo el candidato.
 */
public final class IntencionDeVoto {

    private final String idEstudiante;
    private final String idCandidato;

    public IntencionDeVoto(String idEstudiante, String idCandidato) {
        this.idEstudiante = exigir(idEstudiante, "El identificador del estudiante es obligatorio.");
        this.idCandidato = exigir(idCandidato, "El identificador del candidato es obligatorio.");
    }

    public Papeleta privatizar(LocalDateTime momento) {
        if (momento == null) {
            throw new ReglaDeNegocioVioladaException("La fecha del voto es obligatoria.");
        }
        return Papeleta.anonima(idCandidato, momento);
    }

    public String idEstudiante() {
        return idEstudiante;
    }

    public String idCandidato() {
        return idCandidato;
    }

    private static String exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
        return valor.trim();
    }
}
