package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.dominio.Papeleta;

@Component
@Profile("mysql")
public class UrnaRepositoryMysql implements UrnaRepository {

    private final JdbcTemplate jdbc;

    UrnaRepositoryMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void depositar(Papeleta papeleta) {
        jdbc.update("""
                INSERT INTO papeletas_voto
                    (id_papeleta, id_candidato, token_anonimo, fecha_hora_registro)
                VALUES (?, ?, ?, ?)
                """,
                papeleta.idPapeleta(),
                papeleta.idCandidato(),
                UUID.randomUUID().toString(),
                papeleta.fechaHoraRegistro());
    }

    @Override
    public long total() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM papeletas_voto", Long.class);
        return total == null ? 0 : total;
    }

    @Override
    public Map<String, Long> conteoPorCandidato() {
        Map<String, Long> conteo = new HashMap<>();
        jdbc.query("""
                SELECT id_candidato, COUNT(*) AS votos
                FROM papeletas_voto
                GROUP BY id_candidato
                """, rs -> {
            conteo.put(rs.getString("id_candidato"), rs.getLong("votos"));
        });
        return conteo;
    }
}
