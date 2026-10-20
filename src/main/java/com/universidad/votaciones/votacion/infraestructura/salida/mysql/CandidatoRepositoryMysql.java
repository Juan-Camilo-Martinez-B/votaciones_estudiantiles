package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.Candidato;

@Component
@Profile("mysql")
public class CandidatoRepositoryMysql implements CandidatoRepository {

    private final JdbcTemplate jdbc;

    CandidatoRepositoryMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Candidato candidato) {
        jdbc.update("""
                INSERT INTO candidatos
                    (id_candidato, numero_tarjeton, nombre_candidato, propuesta_resumen)
                VALUES (?, ?, ?, ?) AS nuevo
                ON DUPLICATE KEY UPDATE
                    numero_tarjeton = nuevo.numero_tarjeton,
                    nombre_candidato = nuevo.nombre_candidato,
                    propuesta_resumen = nuevo.propuesta_resumen
                """,
                candidato.idCandidato(),
                candidato.numeroTarjeton(),
                candidato.nombreCandidato(),
                candidato.propuestaResumen());
    }

    @Override
    public boolean existe(String idCandidato) {
        if (idCandidato == null || idCandidato.isBlank()) {
            return false;
        }
        Integer encontrados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM candidatos WHERE id_candidato = ?",
                Integer.class,
                idCandidato.trim());
        return encontrados != null && encontrados > 0;
    }

    @Override
    public List<Candidato> listar() {
        return jdbc.query("""
                SELECT id_candidato, numero_tarjeton, nombre_candidato, propuesta_resumen
                FROM candidatos
                ORDER BY numero_tarjeton
                """, (rs, fila) -> new Candidato(
                rs.getString("id_candidato"),
                rs.getString("numero_tarjeton"),
                rs.getString("nombre_candidato"),
                rs.getString("propuesta_resumen")));
    }
}
