package com.universidad.votaciones.votacion.infraestructura.salida.memoria;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.dominio.JornadaElectoral;

public class JornadaEnMemoria implements JornadaRepository {

    private JornadaElectoral actual;

    @Override
    public synchronized JornadaElectoral obtener() {
        return actual == null ? JornadaElectoral.nueva() : actual;
    }

    @Override
    public synchronized void guardar(JornadaElectoral jornada) {
        this.actual = jornada;
    }
}
