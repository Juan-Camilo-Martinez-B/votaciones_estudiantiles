package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.dominio.Participacion;

@Component
@Profile("mysql")
public class ParticipacionRepositoryMysql implements ParticipacionRepository {

    private static final RowMapper<Participacion> FILA = (rs, numero) -> {
        Timestamp marca = rs.getTimestamp("fecha_hora_peticion");
        return Participacion.reconstruir(
                rs.getString("llave_idempotencia"),
                rs.getString("id_estudiante"),
                rs.getString("token_anonimo"),
                marca == null ? null : marca.toLocalDateTime());
    };

    private final JdbcTemplate jdbc;

    ParticipacionRepositoryMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Participacion> buscarPorLlave(String llaveIdempotencia) {
        if (llaveIdempotencia == null || llaveIdempotencia.isBlank()) {
            return Optional.empty();
        }
        List<Participacion> filas = jdbc.query("""
                SELECT llave_idempotencia, id_estudiante, token_anonimo, fecha_hora_peticion
                FROM transacciones_voto_idempotente
                WHERE llave_idempotencia = ?
                """, FILA, llaveIdempotencia.trim());
        return filas.stream().findFirst();
    }

    @Override
    public boolean yaVoto(String idEstudiante) {
        if (idEstudiante == null || idEstudiante.isBlank()) {
            return false;
        }
        Integer encontrados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM transacciones_voto_idempotente WHERE id_estudiante = ?",
                Integer.class,
                idEstudiante.trim());
        return encontrados != null && encontrados > 0;
    }

    @Override
    public void guardar(Participacion participacion) {
        jdbc.update("""
                INSERT INTO transacciones_voto_idempotente
                    (llave_idempotencia, id_estudiante, token_anonimo, fecha_hora_peticion, estado)
                VALUES (?, ?, ?, ?, ?)
                """,
                participacion.llaveIdempotencia(),
                participacion.idEstudiante(),
                participacion.tokenRecibo(),
                participacion.fechaHora(),
                "PROCESADO");
    }
}
