package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

/**
 * Enumeración que define las acciones auditables del sistema de votaciones.
 *
 * <p>Centralizar las acciones en un enum evita el uso de strings mágicos,
 * facilita la búsqueda/filtrado en logs y garantiza coherencia semántica
 * a lo largo de toda la aplicación (principio DRY).
 */
public enum AccionAuditoria {

    // ─── Autenticación ────────────────────────────────────────────
    /** Usuario inició sesión correctamente. */
    LOGIN_EXITOSO,

    /** Intento de autenticación fallido (credenciales incorrectas). */
    LOGIN_FALLIDO,

    /** Usuario cerró sesión. */
    LOGOUT,

    // ─── Votaciones ───────────────────────────────────────────────
    /** Voto registrado con éxito. */
    VOTO_REGISTRADO,

    /** Intento de votar duplicado (voto ya emitido). */
    VOTO_DUPLICADO_RECHAZADO,

    /** Voto rechazado por candidato inválido o proceso cerrado. */
    VOTO_RECHAZADO,

    // ─── Administración de procesos ───────────────────────────────
    /** Proceso electoral creado. */
    PROCESO_CREADO,

    /** Proceso electoral actualizado. */
    PROCESO_ACTUALIZADO,

    /** Proceso electoral eliminado. */
    PROCESO_ELIMINADO,

    /** Proceso electoral abierto (inicio de votación). */
    PROCESO_ABIERTO,

    /** Proceso electoral cerrado (fin de votación). */
    PROCESO_CERRADO,

    // ─── Administración de candidatos ─────────────────────────────
    /** Candidato registrado en el proceso. */
    CANDIDATO_REGISTRADO,

    /** Candidato eliminado del proceso. */
    CANDIDATO_ELIMINADO,

    // ─── Administración de usuarios ───────────────────────────────
    /** Usuario creado en el sistema. */
    USUARIO_CREADO,

    /** Perfil de usuario modificado. */
    USUARIO_ACTUALIZADO,

    /** Usuario deshabilitado en el sistema. */
    USUARIO_DESHABILITADO,

    // ─── Consultas y reportes ─────────────────────────────────────
    /** Consulta de resultados de votación. */
    RESULTADOS_CONSULTADOS,

    /** Reporte generado/exportado. */
    REPORTE_GENERADO,

    // ─── Seguridad ────────────────────────────────────────────────
    /** Acceso a recurso denegado por permisos insuficientes. */
    ACCESO_DENEGADO,

    /** Operación no autorizada detectada. */
    OPERACION_NO_AUTORIZADA
}
