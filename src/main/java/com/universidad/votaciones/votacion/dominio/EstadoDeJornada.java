package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

public record EstadoDeJornada(String estado, LocalDate dia, LocalTime apertura, LocalTime cierre) {
}
