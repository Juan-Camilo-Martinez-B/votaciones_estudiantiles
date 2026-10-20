package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;

@Component
public class RegistroDeAuditoriaAdaptador implements RegistroDeAuditoria {

    private static final Logger log = LoggerFactory.getLogger(RegistroDeAuditoriaAdaptador.class);

    private final PublicadorAuditoria auditoria;

    public RegistroDeAuditoriaAdaptador(PublicadorAuditoria auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void exito(String actor, HechoElectoral hecho, String detalle) {
        try {
            auditoria.publicarExitoso(actor, traducir(hecho), detalle, "127.0.0.1", "VOTACION");
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar la auditoría de {}: {}", hecho, e.getMessage());
        }
    }

    @Override
    public void fallo(String actor, HechoElectoral hecho, String detalle) {
        try {
            auditoria.publicarFallido(actor, traducir(hecho), detalle, "127.0.0.1", "VOTACION");
        } catch (RuntimeException e) {
            log.warn("No se pudo registrar el fallo de auditoría de {}: {}", hecho, e.getMessage());
        }
    }

    private AccionAuditoria traducir(HechoElectoral hecho) {
        return switch (hecho) {
            case VOTO_REGISTRADO, VOTO_REINTENTO -> AccionAuditoria.VOTO_REGISTRADO;
            case VOTO_DUPLICADO -> AccionAuditoria.VOTO_DUPLICADO_RECHAZADO;
            case VOTO_RECHAZADO -> AccionAuditoria.VOTO_RECHAZADO;
            case JORNADA_INICIADA -> AccionAuditoria.PROCESO_ABIERTO;
            case JORNADA_CERRADA -> AccionAuditoria.PROCESO_CERRADO;
            case JORNADA_RECHAZADA, PADRON_CARGADO -> AccionAuditoria.PROCESO_ACTUALIZADO;
            case CANDIDATO_REGISTRADO -> AccionAuditoria.CANDIDATO_REGISTRADO;
            case RESULTADOS_CONSULTADOS -> AccionAuditoria.RESULTADOS_CONSULTADOS;
            case USUARIO_CREADO -> AccionAuditoria.USUARIO_CREADO;
            case USUARIO_ACTUALIZADO -> AccionAuditoria.USUARIO_ACTUALIZADO;
        };
    }
}
