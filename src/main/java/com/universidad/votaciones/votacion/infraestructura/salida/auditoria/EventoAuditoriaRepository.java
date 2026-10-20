package com.universidad.votaciones.votacion.infraestructura.salida.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link EventoAuditoria}.
 *
 * <p>Extiende {@link JpaRepository} para heredar las operaciones CRUD estándar.
 * Los métodos adicionales permiten consultas de auditoría frecuentes sin
 * necesidad de escribir SQL manual, respetando el principio DRY.
 *
 * <p>Spring Data JPA genera la implementación en tiempo de ejecución (DIP).
 */
@Repository
public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {

    /**
     * Encuentra todos los eventos asociados a un usuario específico,
     * ordenados del más reciente al más antiguo.
     *
     * @param usuarioId ID del usuario
     * @return lista de eventos del usuario
     */
    List<EventoAuditoria> findByUsuarioIdOrderByTimestampDesc(String usuarioId);

    /**
     * Encuentra todos los eventos de un tipo de acción específica.
     *
     * @param accion tipo de acción a filtrar
     * @return lista de eventos de esa acción
     */
    List<EventoAuditoria> findByAccionOrderByTimestampDesc(AccionAuditoria accion);

    /**
     * Encuentra eventos en un rango de tiempo.
     *
     * @param desde instante de inicio del rango (inclusive)
     * @param hasta instante de fin del rango (inclusive)
     * @return lista de eventos en el rango
     */
    @Query("SELECT e FROM EventoAuditoria e WHERE e.timestamp BETWEEN :desde AND :hasta ORDER BY e.timestamp DESC")
    List<EventoAuditoria> findByRangoFecha(@Param("desde") Instant desde,
                                            @Param("hasta") Instant hasta);

    /**
     * Encuentra los eventos de un usuario filtrados por acción.
     *
     * @param usuarioId ID del usuario
     * @param accion    tipo de acción
     * @return lista filtrada de eventos
     */
    List<EventoAuditoria> findByUsuarioIdAndAccionOrderByTimestampDesc(String usuarioId,
                                                                        AccionAuditoria accion);

    /**
     * Cuenta los eventos fallidos de un usuario en los últimos N minutos
     * (útil para detección de ataques de fuerza bruta).
     *
     * @param usuarioId ID del usuario
     * @param accion    acción a contar (ej. LOGIN_FALLIDO)
     * @param desde     instante de inicio del conteo
     * @return cantidad de eventos
     */
    @Query("SELECT COUNT(e) FROM EventoAuditoria e " +
           "WHERE e.usuarioId = :usuarioId AND e.accion = :accion AND e.timestamp >= :desde")
    long contarEventosPorUsuarioDesde(@Param("usuarioId") String usuarioId,
                                       @Param("accion") AccionAuditoria accion,
                                       @Param("desde") Instant desde);
}
