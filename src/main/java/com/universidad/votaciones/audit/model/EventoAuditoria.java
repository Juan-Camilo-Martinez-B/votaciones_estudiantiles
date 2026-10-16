package com.universidad.votaciones.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio que representa un evento de auditoría en el sistema de votaciones.
 *
 * <p>Este objeto es, a la vez:
 * <ul>
 *   <li><b>Mensaje AMQP</b>: publicado y consumido como JSON por RabbitMQ.</li>
 *   <li><b>Entidad JPA</b>: persistida en la tabla {@code auditoria_eventos} de PostgreSQL.</li>
 * </ul>
 *
 * <p>Principios aplicados:
 * <ul>
 *   <li><b>Encapsulamiento</b>: todos los atributos son {@code private}, accesibles
 *       solo mediante getters/setters explícitos.</li>
 *   <li><b>Inmutabilidad parcial</b>: el id se auto-genera y el timestamp se fija
 *       en construcción; ambos no tienen setter público.</li>
 *   <li><b>SRP</b>: esta clase modela exclusivamente el evento; la persistencia y
 *       la publicación son responsabilidades de otras capas.</li>
 * </ul>
 *
 * <p>Jackson puede serializar/deserializar esta clase gracias al constructor sin
 * argumentos requerido por JPA y a los getters/setters estándar.
 */
@Entity
@Table(name = "auditoria_eventos")
public class EventoAuditoria implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ─────────────────────────────────────────────────────────────
    //  Atributos
    // ─────────────────────────────────────────────────────────────

    /**
     * Identificador único del evento (UUID v4).
     * Se genera automáticamente en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Instante exacto (UTC) en que ocurrió el evento.
     * Se asigna en construcción y no debe modificarse.
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    /**
     * Identificador del usuario que generó la acción.
     * Puede ser {@code null} para eventos de sistema sin usuario autenticado.
     */
    @Column(name = "usuario_id")
    private String usuarioId;

    /**
     * Tipo de acción auditada (enum {@link AccionAuditoria}).
     * Persistido como cadena de texto para legibilidad directa en BD.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 60)
    private AccionAuditoria accion;

    /**
     * Descripción detallada del evento (contexto, entidades involucradas, etc.).
     * Campo de texto libre, máximo 2000 caracteres.
     */
    @Column(name = "detalles", length = 2000)
    private String detalles;

    /**
     * Dirección IP del cliente que originó la solicitud.
     * Soporta tanto IPv4 como IPv6.
     */
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    /**
     * Ruta del recurso HTTP afectado (endpoint).
     * Ejemplo: {@code /api/v1/votos}
     */
    @Column(name = "recurso", length = 255)
    private String recurso;

    /**
     * Método HTTP utilizado en la solicitud (GET, POST, PUT, DELETE…).
     */
    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    /**
     * Código de respuesta HTTP resultante de la operación.
     * Ejemplo: 200, 400, 403, 500.
     */
    @Column(name = "codigo_respuesta")
    private Integer codigoRespuesta;

    /**
     * Nombre del módulo o servicio interno que generó el evento.
     * Ejemplo: {@code VotoService}, {@code AuthService}.
     */
    @Column(name = "modulo", length = 100)
    private String modulo;

    /**
     * Resultado de la operación: {@code EXITOSO} o {@code FALLIDO}.
     */
    @Column(name = "resultado", length = 10)
    private String resultado;

    // ─────────────────────────────────────────────────────────────
    //  Constructores
    // ─────────────────────────────────────────────────────────────

    /**
     * Constructor sin argumentos requerido por JPA y Jackson.
     * No debe usarse directamente en código de negocio; usar el builder.
     */
    public EventoAuditoria() {
        this.timestamp = Instant.now();
    }

    /**
     * Constructor completo para creación programática del evento.
     *
     * @param usuarioId      ID del usuario que genera el evento
     * @param accion         tipo de acción auditada
     * @param detalles       descripción libre del evento
     * @param ipOrigen       IP del cliente
     * @param recurso        endpoint/recurso afectado
     * @param metodoHttp     método HTTP de la solicitud
     * @param codigoRespuesta código HTTP de la respuesta
     * @param modulo         módulo que genera el evento
     * @param resultado      resultado de la operación
     */
    public EventoAuditoria(String usuarioId,
                            AccionAuditoria accion,
                            String detalles,
                            String ipOrigen,
                            String recurso,
                            String metodoHttp,
                            Integer codigoRespuesta,
                            String modulo,
                            String resultado) {
        this.timestamp = Instant.now();
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.detalles = detalles;
        this.ipOrigen = ipOrigen;
        this.recurso = recurso;
        this.metodoHttp = metodoHttp;
        this.codigoRespuesta = codigoRespuesta;
        this.modulo = modulo;
        this.resultado = resultado;
    }

    // ─────────────────────────────────────────────────────────────
    //  Getters y Setters
    // ─────────────────────────────────────────────────────────────

    /** @return identificador único del evento */
    public UUID getId() {
        return id;
    }

    /** @return instante UTC del evento */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** @return ID del usuario que originó el evento */
    public String getUsuarioId() {
        return usuarioId;
    }

    /** @param usuarioId ID del usuario */
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** @return tipo de acción auditada */
    public AccionAuditoria getAccion() {
        return accion;
    }

    /** @param accion tipo de acción auditada */
    public void setAccion(AccionAuditoria accion) {
        this.accion = accion;
    }

    /** @return descripción detallada del evento */
    public String getDetalles() {
        return detalles;
    }

    /** @param detalles descripción del evento */
    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    /** @return dirección IP del cliente */
    public String getIpOrigen() {
        return ipOrigen;
    }

    /** @param ipOrigen dirección IP del cliente */
    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    /** @return endpoint/recurso afectado */
    public String getRecurso() {
        return recurso;
    }

    /** @param recurso endpoint/recurso afectado */
    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }

    /** @return método HTTP de la solicitud */
    public String getMetodoHttp() {
        return metodoHttp;
    }

    /** @param metodoHttp método HTTP de la solicitud */
    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    /** @return código de respuesta HTTP */
    public Integer getCodigoRespuesta() {
        return codigoRespuesta;
    }

    /** @param codigoRespuesta código de respuesta HTTP */
    public void setCodigoRespuesta(Integer codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    /** @return módulo que generó el evento */
    public String getModulo() {
        return modulo;
    }

    /** @param modulo módulo que generó el evento */
    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    /** @return resultado de la operación (EXITOSO/FALLIDO) */
    public String getResultado() {
        return resultado;
    }

    /** @param resultado resultado de la operación */
    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    // ─────────────────────────────────────────────────────────────
    //  toString (diagnóstico y logging)
    // ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "EventoAuditoria{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", usuarioId='" + usuarioId + '\'' +
                ", accion=" + accion +
                ", ipOrigen='" + ipOrigen + '\'' +
                ", recurso='" + recurso + '\'' +
                ", metodoHttp='" + metodoHttp + '\'' +
                ", codigoRespuesta=" + codigoRespuesta +
                ", modulo='" + modulo + '\'' +
                ", resultado='" + resultado + '\'' +
                '}';
    }
}
