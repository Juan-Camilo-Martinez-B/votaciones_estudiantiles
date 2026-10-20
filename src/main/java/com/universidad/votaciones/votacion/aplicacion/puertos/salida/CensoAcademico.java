package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.List;

public interface CensoAcademico {
    List<String> listarCodigosHabilitados();

    boolean estaHabilitado(String codigoEstudiante);
}
