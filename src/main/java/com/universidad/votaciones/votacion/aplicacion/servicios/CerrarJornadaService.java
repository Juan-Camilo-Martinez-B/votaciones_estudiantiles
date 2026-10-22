package com.universidad.votaciones.votacion.aplicacion.servicios;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CerrarJornadaUseCase;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.dominio.EstadoDeJornada;
import com.universidad.votaciones.votacion.dominio.JornadaElectoral;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;

public class CerrarJornadaService implements CerrarJornadaUseCase {

    private final JornadaRepository jornadas;
    private final RegistroDeAuditoria auditoria;

    public CerrarJornadaService(JornadaRepository jornadas, RegistroDeAuditoria auditoria) {
        this.jornadas = jornadas;
        this.auditoria = auditoria;
    }

    @Override
    public EstadoDeJornada cerrar() {
        JornadaElectoral jornada = jornadas.obtener();
        try {
            jornada.cerrar();
        } catch (ReglaDeNegocioVioladaException e) {
            auditoria.fallo("SISTEMA", HechoElectoral.JORNADA_RECHAZADA, e.getMessage());
            throw e;
        }
        jornadas.guardar(jornada);
        auditoria.exito("SISTEMA", HechoElectoral.JORNADA_CERRADA, "Jornada cerrada.");
        return jornada.describir();
    }
}
