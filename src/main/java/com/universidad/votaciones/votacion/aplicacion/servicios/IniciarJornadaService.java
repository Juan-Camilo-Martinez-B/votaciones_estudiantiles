package com.universidad.votaciones.votacion.aplicacion.servicios;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.IniciarJornadaUseCase;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.Reloj;
import com.universidad.votaciones.votacion.dominio.EstadoDeJornada;
import com.universidad.votaciones.votacion.dominio.JornadaElectoral;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;

public class IniciarJornadaService implements IniciarJornadaUseCase {

    private final JornadaRepository jornadas;
    private final Reloj reloj;
    private final RegistroDeAuditoria auditoria;

    public IniciarJornadaService(JornadaRepository jornadas, Reloj reloj, RegistroDeAuditoria auditoria) {
        this.jornadas = jornadas;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    public EstadoDeJornada iniciar() {
        JornadaElectoral jornada = jornadas.obtener();
        try {
            jornada.iniciar(reloj.ahora());
        } catch (ReglaDeNegocioVioladaException e) {
            auditoria.fallo("SISTEMA", HechoElectoral.JORNADA_RECHAZADA, e.getMessage());
            throw e;
        }
        jornadas.guardar(jornada);
        auditoria.exito("SISTEMA", HechoElectoral.JORNADA_INICIADA,
                "Jornada abierta para el día " + jornada.dia());
        return jornada.describir();
    }
}
