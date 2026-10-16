package com.universidad.votaciones.audit;

import com.universidad.votaciones.audit.model.AccionAuditoria;
import com.universidad.votaciones.audit.model.EventoAuditoria;
import com.universidad.votaciones.audit.model.EventoAuditoriaRepository;
import com.universidad.votaciones.audit.publisher.PublicadorAuditoria;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fachada pública oficial del Módulo de Auditoría y Trazabilidad Transaccional.
 *
 * Expuesta en la raíz del paquete audit para permitir a otros módulos emitir
 * eventos de auditoría y consultar trazas sin acoplarse a RabbitMQ ni a los
 * repositorios internos.
 */
@Component
public class AuditApi {

    private final PublicadorAuditoria publicadorAuditoria;
    private final EventoAuditoriaRepository eventoAuditoriaRepository;

    public AuditApi(PublicadorAuditoria publicadorAuditoria,
                    EventoAuditoriaRepository eventoAuditoriaRepository) {
        this.publicadorAuditoria = publicadorAuditoria;
        this.eventoAuditoriaRepository = eventoAuditoriaRepository;
    }

    /**
     * Publica un evento de auditoría asíncrono hacia el broker de mensajería RabbitMQ.
     */
    public void registrarEvento(EventoAuditoria evento) {
        publicadorAuditoria.publicar(evento);
    }

    /**
     * Publica un evento exitoso de auditoría para un usuario y módulo específico.
     */
    public void registrarExito(String usuarioId, AccionAuditoria accion, String detalles, String modulo) {
        publicadorAuditoria.publicarExitoso(usuarioId, accion, detalles, "127.0.0.1", modulo);
    }

    /**
     * Publica un evento fallido de auditoría para un usuario y módulo específico.
     */
    public void registrarFallo(String usuarioId, AccionAuditoria accion, String detalles, String modulo) {
        publicadorAuditoria.publicarFallido(usuarioId, accion, detalles, "127.0.0.1", modulo);
    }

    /**
     * Registra un evento completo con detalles de petición HTTP y código de respuesta.
     */
    public void registrarEventoHttp(String usuarioId,
                                    AccionAuditoria accion,
                                    String detalles,
                                    String ipOrigen,
                                    String recurso,
                                    String metodoHttp,
                                    Integer codigoRespuesta,
                                    String modulo,
                                    String resultado) {
        publicadorAuditoria.publicar(usuarioId, accion, detalles, ipOrigen, recurso, metodoHttp, codigoRespuesta, modulo, resultado);
    }

    /**
     * Consulta el historial de eventos persistidos de un usuario específico.
     */
    public List<EventoAuditoria> consultarEventosPorUsuario(String usuarioId) {
        return eventoAuditoriaRepository.findByUsuarioIdOrderByTimestampDesc(usuarioId);
    }

    /**
     * Consulta eventos de auditoría filtrados por acción.
     */
    public List<EventoAuditoria> consultarEventosPorAccion(AccionAuditoria accion) {
        return eventoAuditoriaRepository.findByAccionOrderByTimestampDesc(accion);
    }

    /**
     * Consulta el total de eventos de auditoría persistidos.
     */
    public long contarEventos() {
        return eventoAuditoriaRepository.count();
    }
}
