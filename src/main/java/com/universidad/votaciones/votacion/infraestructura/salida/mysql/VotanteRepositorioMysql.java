package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.VotanteRepositorio;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;
import com.universidad.votaciones.votacion.dominio.Votante;

@Component
@Profile("mysql")
public class VotanteRepositorioMysql implements VotanteRepositorio {

    private static final String SELECT = """
            SELECT u.id, u.nombre, u.apellido, u.email,
                   v.codigo_estudiantil, v.programa_academico, v.estado_votante
            FROM usuarios u
            JOIN votantes_estudiantes v ON v.usuario_id = u.id
            """;

    private static final RowMapper<Votante> FILA = (rs, numero) -> new Votante(
            rs.getLong("id"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("codigo_estudiantil"),
            rs.getString("programa_academico"),
            EstadoVotante.valueOf(rs.getString("estado_votante")));

    private final JdbcTemplate jdbc;

    VotanteRepositorioMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Votante guardar(Votante votante, String claveCodificada) {
        long id = insertarUsuario(votante.nombre(), votante.apellido(), votante.email(), claveCodificada);
        jdbc.update("""
                INSERT INTO votantes_estudiantes
                    (usuario_id, codigo_estudiantil, programa_academico, estado_votante)
                VALUES (?, ?, ?, ?)
                """, id, votante.codigoEstudiantil(), votante.programaAcademico(), votante.estadoVotante().name());
        return new Votante(id, votante.nombre(), votante.apellido(), votante.email(),
                votante.codigoEstudiantil(), votante.programaAcademico(), votante.estadoVotante());
    }

    @Override
    public Optional<Votante> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jdbc.query(SELECT + " WHERE u.id = ?", FILA, id).stream().findFirst();
    }

    @Override
    public Optional<Votante> buscarPorCodigo(String codigoEstudiantil) {
        if (codigoEstudiantil == null || codigoEstudiantil.isBlank()) {
            return Optional.empty();
        }
        return jdbc.query(SELECT + " WHERE v.codigo_estudiantil = ?", FILA, codigoEstudiantil.trim())
                .stream().findFirst();
    }

    @Override
    public boolean existeEmail(String email) {
        return contar("SELECT COUNT(*) FROM usuarios u JOIN votantes_estudiantes v ON v.usuario_id = u.id WHERE u.email = ?",
                email);
    }

    @Override
    public boolean existeCodigo(String codigoEstudiantil) {
        return contar("SELECT COUNT(*) FROM votantes_estudiantes WHERE codigo_estudiantil = ?", codigoEstudiantil);
    }

    @Override
    public List<Votante> listar() {
        return jdbc.query(SELECT + " ORDER BY u.id", FILA);
    }

    @Override
    public List<Votante> listarPorEstado(EstadoVotante estado) {
        return jdbc.query(SELECT + " WHERE v.estado_votante = ? ORDER BY u.id", FILA, estado.name());
    }

    @Override
    public Votante actualizarEstado(Long id, EstadoVotante estado) {
        int filas = jdbc.update(
                "UPDATE votantes_estudiantes SET estado_votante = ? WHERE usuario_id = ?",
                estado.name(), id);
        if (filas == 0) {
            throw new UsuarioNoEncontradoException(id);
        }
        jdbc.update("UPDATE usuarios SET fecha_actualizacion = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()), id);
        return buscarPorId(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    private long insertarUsuario(String nombre, String apellido, String email, String claveCodificada) {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        KeyHolder claves = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO usuarios
                        (nombre, apellido, email, password, fecha_creacion, fecha_actualizacion)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, email);
            ps.setString(4, claveCodificada);
            ps.setTimestamp(5, ahora);
            ps.setTimestamp(6, ahora);
            return ps;
        }, claves);
        Number id = claves.getKey();
        if (id == null) {
            throw new IllegalStateException("MySQL no devolvió el id del usuario.");
        }
        return id.longValue();
    }

    private boolean contar(String sql, String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        Integer encontrados = jdbc.queryForObject(sql, Integer.class, valor.trim());
        return encontrados != null && encontrados > 0;
    }
}
