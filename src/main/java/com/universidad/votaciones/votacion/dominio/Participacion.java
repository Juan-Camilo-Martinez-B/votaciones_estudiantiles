package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDateTime;

/**
 * Deja constancia de que el estudiante ya votó y conserva el recibo idempotente.
 * No conoce al candidato.
 */
public final class Participacion {

    private final String llaveIdempotencia;
    private final String idEstudiante;
    private final String tokenRecibo;
    private final LocalDateTime fechaHora;

    private Participacion(String llaveIdempotencia, String idEstudiante, String tokenRecibo, LocalDateTime fechaHora) {
        this.llaveIdempotencia = llaveIdempotencia;
        this.idEstudiante = idEstudiante;
        this.tokenRecibo = tokenRecibo;
        this.fechaHora = fechaHora;
    }

    public static Participacion registrar(String llaveIdempotencia, String idEstudiante,
                                          String tokenRecibo, LocalDateTime fechaHora) {
        return new Participacion(
                exigir(llaveIdempotencia, "La llave de idempotencia es obligatoria."),
                exigir(idEstudiante, "El identificador del estudiante es obligatorio."),
                exigir(tokenRecibo, "El token de recibo es obligatorio."),
                exigirFecha(fechaHora));
    }

    public static Participacion reconstruir(String llaveIdempotencia, String idEstudiante,
                                            String tokenRecibo, LocalDateTime fechaHora) {
        return new Participacion(llaveIdempotencia, idEstudiante, tokenRecibo, fechaHora);
    }

    public String llaveIdempotencia() {
        return llaveIdempotencia;
    }

    public String idEstudiante() {
        return idEstudiante;
    }

    public String tokenRecibo() {
        return tokenRecibo;
    }

    public LocalDateTime fechaHora() {
        return fechaHora;
    }

    private static String exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
        return valor.trim();
    }

    private static LocalDateTime exigirFecha(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            throw new ReglaDeNegocioVioladaException("La fecha del voto es obligatoria.");
        }
        return fechaHora;
    }
}
