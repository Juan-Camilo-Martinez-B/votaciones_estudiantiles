package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import com.universidad.votaciones.votacion.dominio.HechoElectoral;

public interface RegistroDeAuditoria {
    void exito(String actor, HechoElectoral hecho, String detalle);

    void fallo(String actor, HechoElectoral hecho, String detalle);
}
