package com.universidad.votaciones.votacion.dominio;

public record Candidato(String idCandidato, String numeroTarjeton, String nombreCandidato, String propuestaResumen) {

    public Candidato {
        idCandidato = exigir(idCandidato, "El identificador del candidato es obligatorio.");
        numeroTarjeton = exigir(numeroTarjeton, "El número del tarjetón es obligatorio.");
        nombreCandidato = exigir(nombreCandidato, "El nombre del candidato es obligatorio.");
        propuestaResumen = propuestaResumen == null ? "" : propuestaResumen.trim();
    }

    private static String exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
        return valor.trim();
    }
}
