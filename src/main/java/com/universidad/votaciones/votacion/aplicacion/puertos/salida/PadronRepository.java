package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.Collection;

public interface PadronRepository {
    void reemplazar(Collection<String> codigos);

    boolean contiene(String codigoEstudiante);

    int cantidad();
}
