package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Worker asíncrono que consume eventos de auditoría desde RabbitMQ
 * y los persiste en PostgreSQL.
 *
 * <p>Este componente implementa el patrón <b>Consumer / Worker</b>:
 * escucha pasivamente la cola {@code auditoria.cola} y procesa cada
 * mensaje en un hilo separado del hilo de la petición HTTP principal,
 * garantizando que la lógica de persistencia no impacte el rendimiento
 * de la aplicación (requisito p95 &lt; 2 s).
 *
 * <h3>Flujo de procesamiento:</h3>
 * <ol>
 *   <li>RabbitMQ entrega un mensaje JSON al listener.</li>
 *   <li>Jackson deserializa el JSON a un objeto {@link EventoAuditoria}.</li>
 *   <li>El worker valida el objeto y lo persiste en BD.</li>
 *   <li>Si la persistencia falla, se lanza una excepción para que RabbitMQ
 *       pueda reintentar o enviar el mensaje a la DLQ.</li>
 * </ol>
 *
 * <h3>Garantías de fiabilidad:</h3>
 * <ul>
 *   <li><b>Acknowledge manual implícito</b>: Spring AMQP solo hace ACK cuando el
 *       método retorna sin excepción. Un fallo lanza NACK, activando la DLQ.</li>
 *   <li><b>Transaccionalidad</b>: la persistencia se realiza dentro de una
 *       transacción para garantizar atomicidad.</li>
 * </ul>
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li><b>SRP</b>: única responsabilidad → consumir y persistir eventos.</li>
 *   <li><b>DIP</b>: depende de la abstracción {@link EventoAuditoriaRepository}.</li>
 * </ul>
 */
@Component
public class WorkerAuditoria {

    private static final Logger log = LoggerFactory.getLogger(WorkerAuditoria.class);

    // ─────────────────────────────────────────────────────────────
    //  Dependencias
    // ─────────────────────────────────────────────────────────────

    /**
     * Repositorio JPA para persistir los eventos de auditoría.
     * Inyectado por constructor para facilitar pruebas unitarias.
     */
    private final EventoAuditoriaRepository auditoriaRepository;

    // ─────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────

    /**
     * Constructor con inyección de dependencias.
     *
     * @param auditoriaRepository repositorio de eventos de auditoría
     */
    public WorkerAuditoria(EventoAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // ─────────────────────────────────────────────────────────────
    //  Listener principal
    // ─────────────────────────────────────────────────────────────

    /**
     * Listener principal que consume mensajes de la cola de auditoría.
     *
     * <p>La anotación {@link RabbitListener} registra este método como consumidor
     * de la cola {@value RabbitMQConfig#AUDIT_QUEUE}. Jackson deserializa
     * automáticamente el payload JSON al tipo {@link EventoAuditoria} gracias
     * al {@code MessageConverter} configurado en {@link RabbitMQConfig}.
     *
     * <p>La anotación {@link Transactional} garantiza que la persistencia
     * se realice dentro de una transacción de BD. Si ocurre un error:
     * <ul>
     *   <li>La transacción hace rollback.</li>
     *   <li>Se lanza una excepción.</li>
     *   <li>RabbitMQ recibe un NACK y puede redirigir el mensaje a la DLQ.</li>
     * </ul>
     *
     * @param evento objeto {@link EventoAuditoria} deserializado del mensaje JSON
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    @Transactional
    public void procesarEvento(@Payload EventoAuditoria evento) {

        log.debug("[Worker-Auditoría] Mensaje recibido desde la cola '{}': accion={}, usuario={}",
                RabbitMQConfig.AUDIT_QUEUE, evento.getAccion(), evento.getUsuarioId());

        // ── Validación defensiva del payload ────────────────────────────
        if (evento.getAccion() == null) {
            log.warn("[Worker-Auditoría] Evento recibido sin acción definida. " +
                     "Usuario: {}. El mensaje será descartado.", evento.getUsuarioId());
            /*
             * Al retornar sin excepción, RabbitMQ hace ACK del mensaje
             * (lo descarta). Esto es intencional para mensajes mal formados
             * que no deben reintentarse indefinidamente.
             */
            return;
        }

        // ── Persistencia en PostgreSQL ───────────────────────────────────
        try {
            EventoAuditoria guardado = auditoriaRepository.save(evento);

            log.info("[Worker-Auditoría] Evento persistido correctamente. " +
                     "id={}, accion={}, usuario={}, ip={}, resultado={}",
                     guardado.getId(),
                     guardado.getAccion(),
                     guardado.getUsuarioId(),
                     guardado.getIpOrigen(),
                     guardado.getResultado());

        } catch (DataAccessException ex) {
            /*
             * Error de base de datos → lanzamos RuntimeException para forzar
             * un NACK en RabbitMQ. Según la configuración de la cola, el
             * mensaje será redirigido a la DLQ (auditoria.cola.dlq) para
             * análisis posterior sin perderse.
             */
            log.error("[Worker-Auditoría] Error al persistir evento en BD. " +
                      "accion={}, usuario={} — Causa: {}",
                      evento.getAccion(), evento.getUsuarioId(), ex.getMessage(), ex);

            throw new WorkerAuditoriaException(
                    "Fallo al persistir EventoAuditoria en base de datos. " +
                    "El mensaje será enviado a la DLQ.", ex);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Listener de Dead-Letter Queue (DLQ)
    // ─────────────────────────────────────────────────────────────

    /**
     * Listener de la Dead-Letter Queue (DLQ).
     *
     * <p>Procesa los mensajes que no pudieron ser entregados en la cola principal
     * después de los reintentos configurados. En este punto solo se registran
     * en el log para revisión manual por parte del equipo de operaciones.
     *
     * <p>Este listener puede extenderse para enviar alertas, notificaciones
     * por email o guardar los eventos en una tabla de errores.
     *
     * @param evento objeto {@link EventoAuditoria} que no pudo procesarse
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_DLQ)
    public void procesarEventoFallido(@Payload EventoAuditoria evento) {
        log.error("[Worker-Auditoría][DLQ] Mensaje en Dead-Letter Queue. " +
                  "Requiere revisión manual. accion={}, usuario={}, timestamp={}",
                  evento.getAccion(), evento.getUsuarioId(), evento.getTimestamp());

        /*
         * EXTENSIÓN FUTURA: aquí se puede integrar:
         *   - Envío de alerta al equipo (email, Slack, PagerDuty).
         *   - Persistencia en tabla de eventos fallidos para auditoría diferida.
         *   - Reintento manual con corrección del payload.
         */
    }
}
