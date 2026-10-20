package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.sql.Date;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.dominio.EstadoJornada;
import com.universidad.votaciones.votacion.dominio.JornadaElectoral;

@Component
@Profile("mysql")
public class JornadaRepositoryMysql implements JornadaRepository {

    static final String ID = "actual";

    private final JdbcTemplate jdbc;

    JornadaRepositoryMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public JornadaElectoral obtener() {
        List<JornadaElectoral> filas = jdbc.query(
                "SELECT estado, dia FROM jornada_electoral WHERE id = ?",
                (rs, fila) -> {
                    Date dia = rs.getDate("dia");
                    String estado = rs.getString("estado");
                    return JornadaElectoral.reconstruir(
                            estado == null ? null : EstadoJornada.valueOf(estado),
                            dia == null ? null : dia.toLocalDate());
                },
                ID);
        return filas.isEmpty() ? JornadaElectoral.nueva() : filas.getFirst();
    }

    @Override
    public void guardar(JornadaElectoral jornada) {
        jdbc.update("""
                INSERT INTO jornada_electoral (id, estado, dia) VALUES (?, ?, ?) AS nueva
                ON DUPLICATE KEY UPDATE estado = nueva.estado, dia = nueva.dia
                """, ID, jornada.estado().name(), jornada.dia());
    }
}
