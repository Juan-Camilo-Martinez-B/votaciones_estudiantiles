package com.universidad.votaciones.votacion.infraestructura.salida.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;

@Component
@Profile("mysql")
public class PadronRepositoryMysql implements PadronRepository {

    private final JdbcTemplate jdbc;

    PadronRepositoryMysql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void reemplazar(Collection<String> codigos) {
        jdbc.update("DELETE FROM padron_electoral");
        List<Object[]> lote = new ArrayList<>();
        if (codigos != null) {
            for (String codigo : codigos) {
                if (codigo != null && !codigo.isBlank()) {
                    lote.add(new Object[] {codigo.trim()});
                }
            }
        }
        if (!lote.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO padron_electoral (codigo_estudiante) VALUES (?)", lote);
        }
    }

    @Override
    public boolean contiene(String codigoEstudiante) {
        if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
            return false;
        }
        Integer encontrados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM padron_electoral WHERE codigo_estudiante = ?",
                Integer.class,
                codigoEstudiante.trim());
        return encontrados != null && encontrados > 0;
    }

    @Override
    public int cantidad() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM padron_electoral", Integer.class);
        return total == null ? 0 : total;
    }
}
