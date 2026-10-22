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

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.AdministradorRepositorio;
import com.universidad.votaciones.votacion.dominio.Administrador;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;

@Component
@Profile("mysql")
public class AdministradorRepositorioMysql implements AdministradorRepositorio {

    private static final String SELECT = """
            SELECT u.id, u.nombre, u.apellido, u.email, a.cargo, a.activo
            FROM usuarios u
            JOIN usuarios_administradores a ON a.usuario_id = u.id
            """;

    private static final RowMapper<Administrador> FILA = (rs, numero) -> new Administrador(
            rs.getLong("id"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("cargo"),
            rs.getBoolean("activo"));

    private final JdbcTemplate jdbc;

    AdministradorRepositorioMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Administrador guardar(Administrador administrador, String claveCodificada) {
        long id = insertarUsuario(administrador.nombre(), administrador.apellido(),
                administrador.email(), claveCodificada);
        jdbc.update("""
                INSERT INTO usuarios_administradores (usuario_id, cargo, activo)
                VALUES (?, ?, ?)
                """, id, administrador.cargo(), administrador.activo());
        return new Administrador(id, administrador.nombre(), administrador.apellido(),
                administrador.email(), administrador.cargo(), administrador.activo());
    }

    @Override
    public Optional<Administrador> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jdbc.query(SELECT + " WHERE u.id = ?", FILA, id).stream().findFirst();
    }

    @Override
    public boolean existeEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        Integer encontrados = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM usuarios u
                JOIN usuarios_administradores a ON a.usuario_id = u.id
                WHERE u.email = ?
                """, Integer.class, email.trim());
        return encontrados != null && encontrados > 0;
    }

    @Override
    public List<Administrador> listar() {
        return jdbc.query(SELECT + " ORDER BY u.id", FILA);
    }

    @Override
    public List<Administrador> listarActivos() {
        return jdbc.query(SELECT + " WHERE a.activo = 1 ORDER BY u.id", FILA);
    }

    @Override
    public Administrador actualizarActivo(Long id, boolean activo) {
        int filas = jdbc.update(
                "UPDATE usuarios_administradores SET activo = ? WHERE usuario_id = ?",
                activo, id);
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
}
