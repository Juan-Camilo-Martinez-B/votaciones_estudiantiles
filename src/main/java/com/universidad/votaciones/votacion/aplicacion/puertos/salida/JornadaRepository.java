package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import com.universidad.votaciones.votacion.dominio.JornadaElectoral;

public interface JornadaRepository {
    JornadaElectoral obtener();

    void guardar(JornadaElectoral jornada);
}
