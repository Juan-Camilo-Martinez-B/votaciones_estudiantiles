package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * La votación dura un solo día, de 08:00 a 16:00.
 * Abrirla y cerrarla son operaciones distintas: no se reabre después de cerrar.
 */
public class JornadaElectoral {

    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE = LocalTime.of(16, 0);

    private EstadoJornada estado;
    private LocalDate dia;

    private JornadaElectoral(EstadoJornada estado, LocalDate dia) {
        this.estado = estado;
        this.dia = dia;
    }

    public static JornadaElectoral nueva() {
        return new JornadaElectoral(EstadoJornada.NO_INICIADA, null);
    }

    public static JornadaElectoral reconstruir(EstadoJornada estado, LocalDate dia) {
        return new JornadaElectoral(estado == null ? EstadoJornada.NO_INICIADA : estado, dia);
    }

    public void iniciar(LocalDateTime ahora) {
        exigirMomento(ahora);
        if (estado == EstadoJornada.ABIERTA) {
            throw new ReglaDeNegocioVioladaException("La jornada ya está abierta.");
        }
        if (estado == EstadoJornada.CERRADA) {
            throw new ReglaDeNegocioVioladaException(
                    "La jornada ya fue cerrada. La votación dura un solo día.");
        }
        exigirHorario(ahora, "La jornada solo puede iniciar entre las 08:00 y las 16:00.");
        this.dia = ahora.toLocalDate();
        this.estado = EstadoJornada.ABIERTA;
    }

    public void cerrar() {
        if (estado != EstadoJornada.ABIERTA) {
            throw new ReglaDeNegocioVioladaException("No hay una jornada abierta para cerrar.");
        }
        this.estado = EstadoJornada.CERRADA;
    }

    public void asegurarPuedeRecibirVotos(LocalDateTime ahora) {
        exigirMomento(ahora);
        if (estado == EstadoJornada.NO_INICIADA) {
            throw new ReglaDeNegocioVioladaException("La votación no ha sido iniciada.");
        }
        if (estado == EstadoJornada.CERRADA) {
            throw new ReglaDeNegocioVioladaException("La votación ya fue cerrada.");
        }
        if (!ahora.toLocalDate().equals(dia)) {
            throw new ReglaDeNegocioVioladaException("La votación dura un solo día (" + dia + ").");
        }
        exigirHorario(ahora, "La votación solo recibe votos entre las 08:00 y las 16:00.");
    }

    public EstadoDeJornada describir() {
        return new EstadoDeJornada(estado.name(), dia, APERTURA, CIERRE);
    }

    public EstadoJornada estado() {
        return estado;
    }

    public LocalDate dia() {
        return dia;
    }

    private static void exigirMomento(LocalDateTime ahora) {
        if (ahora == null) {
            throw new ReglaDeNegocioVioladaException("Se requiere la hora actual para evaluar la jornada.");
        }
    }

    private static void exigirHorario(LocalDateTime ahora, String mensaje) {
        LocalTime hora = ahora.toLocalTime();
        if (hora.isBefore(APERTURA) || hora.isAfter(CIERRE)) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
    }
}
