package com.universidad.votaciones.audit.publisher;

import com.universidad.votaciones.audit.config.RabbitMQConfig;
import com.universidad.votaciones.audit.model.AccionAuditoria;
import com.universidad.votaciones.audit.model.EventoAuditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publicador (Producer) de eventos de auditoría en RabbitMQ.
 *
 * <p>Este servicio actúa como el <b>punto de entrada único</b> para emitir
 * trazas de auditoría. Su diseño garantiza:
 * <ul>
 *   <li><b>Desacoplamiento</b>: la lógica de negocio (VotoService, AuthService, etc.)
 *       no conoce los detalles de RabbitMQ; solo invoca a este publicador.</li>
 *   <li><b>Fire-and-forget asíncrono</b>: la publicación en la cola no bloquea el
 *       hilo principal, contribuyendo al requisito de rendimiento p95 &lt; 2 s.</li>
 *   <li><b>Resiliencia</b>: los errores de conexión se capturan y loguean sin
 *       propagar excepciones a la capa de negocio.</li>
 * </ul>
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li><b>SRP</b>: única responsabilidad → publicar eventos en el exchange.</li>
 *   <li><b>DIP</b>: depende de la abstracción {@link RabbitTemplate}, no de una
 *       implementación concreta de conexión AMQP.</li>
 *   <li><b>OCP</b>: nuevos métodos de publicación pueden añadirse sin modificar
 *       los existentes.</li>
 * </ul>
 */
@Service
public class PublicadorAuditoria {

    private static final Logger log = LoggerFactory.getLogger(PublicadorAuditoria.class);

    /** Valor del campo resultado cuando la operación fue exitosa. */
    public static final String RESULTADO_EXITOSO = "EXITOSO";

    /** Valor del campo resultado cuando la operación falló. */
    public static final String RESULTADO_FALLIDO = "FALLIDO";

    // ─────────────────────────────────────────────────────────────
    //  Dependencias
    // ─────────────────────────────────────────────────────────────

    /**
     * Template de RabbitMQ configurado con el conversor JSON.
     * Inyectado por constructor para facilitar pruebas unitarias (testabilidad).
     */
    private final RabbitTemplate rabbitTemplate;

    // ─────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────

    /**
     * Constructor con inyección de dependencias.
     *
     * @param rabbitTemplate template AMQP configurado con conversor JSON
     */
    public PublicadorAuditoria(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─────────────────────────────────────────────────────────────
    //  Métodos públicos de publicación
    // ─────────────────────────────────────────────────────────────

    /**
     * Publica un {@link EventoAuditoria} en el exchange de auditoría.
     *
     * <p>El mensaje se serializa automáticamente a JSON gracias al
     * {@link org.springframework.amqp.support.converter.Jackson2JsonMessageConverter}
     * configurado en {@link RabbitMQConfig}.
     *
     * <p>La operación es <b>fire-and-forget</b>: si la publicación falla, el error
     * se registra en el log pero NO se relanza, preservando así la disponibilidad
     * del flujo principal de la aplicación.
     *
     * @param evento objeto de auditoría a publicar (nunca {@code null})
     */
    public void publicar(EventoAuditoria evento) {
        if (evento == null) {
            log.warn("[Auditoría] Se intentó publicar un evento nulo. Operación ignorada.");
            return;
        }

        try {
            log.debug("[Auditoría] Publicando evento: accion={}, usuario={}, ip={}",
                    evento.getAccion(), evento.getUsuarioId(), evento.getIpOrigen());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.AUDIT_EXCHANGE,
                    RabbitMQConfig.AUDIT_ROUTING_KEY,
                    evento
            );

            log.info("[Auditoría] Evento publicado correctamente: accion={}, usuario={}, modulo={}",
                    evento.getAccion(), evento.getUsuarioId(), evento.getModulo());

        } catch (AmqpException ex) {
            // No propagamos la excepción para no degradar el rendimiento de la app principal.
            // El log de error es suficiente para alertas y monitoreo.
            log.error("[Auditoría] Error al publicar evento en RabbitMQ. " +
                      "accion={}, usuario={} — Causa: {}",
                      evento.getAccion(), evento.getUsuarioId(), ex.getMessage(), ex);
        }
    }

    /**
     * Método de conveniencia para publicar eventos de auditoría construyendo
     * el objeto internamente. Reduce el código boilerplate en los servicios clientes.
     *
     * @param usuarioId       ID del usuario que origina el evento
     * @param accion          tipo de acción auditada
     * @param detalles        descripción libre del evento
     * @param ipOrigen        dirección IP del cliente
     * @param recurso         endpoint o recurso afectado
     * @param metodoHttp      método HTTP de la solicitud
     * @param codigoRespuesta código de respuesta HTTP
     * @param modulo          módulo/servicio que genera el evento
     * @param resultado       {@link #RESULTADO_EXITOSO} o {@link #RESULTADO_FALLIDO}
     */
    public void publicar(String usuarioId,
                         AccionAuditoria accion,
                         String detalles,
                         String ipOrigen,
                         String recurso,
                         String metodoHttp,
                         Integer codigoRespuesta,
                         String modulo,
                         String resultado) {

        EventoAuditoria evento = new EventoAuditoria(
                usuarioId, accion, detalles, ipOrigen,
                recurso, metodoHttp, codigoRespuesta, modulo, resultado
        );
        publicar(evento);
    }

    /**
     * Atajo para publicar un evento exitoso con los campos mínimos requeridos.
     *
     * @param usuarioId ID del usuario
     * @param accion    tipo de acción
     * @param detalles  descripción del evento
     * @param ipOrigen  IP del cliente
     * @param modulo    módulo que genera el evento
     */
    public void publicarExitoso(String usuarioId,
                                 AccionAuditoria accion,
                                 String detalles,
                                 String ipOrigen,
                                 String modulo) {
        publicar(usuarioId, accion, detalles, ipOrigen,
                null, null, null, modulo, RESULTADO_EXITOSO);
    }

    /**
     * Atajo para publicar un evento fallido con los campos mínimos requeridos.
     *
     * @param usuarioId ID del usuario (puede ser {@code null} si no autenticado)
     * @param accion    tipo de acción
     * @param detalles  descripción del error o motivo del fallo
     * @param ipOrigen  IP del cliente
     * @param modulo    módulo que genera el evento
     */
    public void publicarFallido(String usuarioId,
                                 AccionAuditoria accion,
                                 String detalles,
                                 String ipOrigen,
                                 String modulo) {
        publicar(usuarioId, accion, detalles, ipOrigen,
                null, null, null, modulo, RESULTADO_FALLIDO);
    }
}
