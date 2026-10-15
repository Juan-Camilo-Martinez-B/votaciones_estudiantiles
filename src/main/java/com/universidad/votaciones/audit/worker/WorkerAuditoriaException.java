package com.universidad.votaciones.audit.worker;

import java.io.Serial;

/**
 * Excepción de dominio lanzada por el {@link WorkerAuditoria} cuando
 * ocurre un error irrecuperable durante el procesamiento de un evento.
 *
 * <p>Al extender {@link RuntimeException}, esta excepción:
 * <ul>
 *   <li>No requiere declaración en la firma del método (unchecked).</li>
 *   <li>Activa el NACK de RabbitMQ, enviando el mensaje a la DLQ.</li>
 *   <li>Provoca el rollback de la transacción JPA activa (si aplica).</li>
 * </ul>
 *
 * <p>Principio LSP: hereda el comportamiento completo de {@link RuntimeException}
 * sin restricciones, añadiendo solo contexto semántico del dominio de auditoría.
 */
public class WorkerAuditoriaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje de error descriptivo.
     *
     * @param mensaje descripción del error ocurrido en el worker
     */
    public WorkerAuditoriaException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje de error y causa raíz.
     *
     * @param mensaje descripción del error ocurrido en el worker
     * @param causa   excepción original que causó el fallo
     */
    public WorkerAuditoriaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
