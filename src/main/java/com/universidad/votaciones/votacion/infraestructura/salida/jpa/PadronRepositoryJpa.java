package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class PadronRepositoryJpa implements PadronRepository {

    private final PadronJpaRepo repo;

    PadronRepositoryJpa(PadronJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public void reemplazar(Collection<String> codigos) {
        repo.deleteAllInBatch();
        List<PadronJpa> filas = new ArrayList<>();
        if (codigos != null) {
            for (String codigo : codigos) {
                if (codigo == null || codigo.isBlank()) {
                    continue;
                }
                PadronJpa fila = new PadronJpa();
                fila.codigoEstudiante = codigo.trim();
                filas.add(fila);
            }
        }
        repo.saveAll(filas);
    }

    @Override
    public boolean contiene(String codigoEstudiante) {
        return codigoEstudiante != null && !codigoEstudiante.isBlank()
                && repo.existsById(codigoEstudiante.trim());
    }

    @Override
    public int cantidad() {
        return (int) repo.count();
    }
}

@Entity
@Table(name = "padron_electoral")
class PadronJpa {
    @Id
    String codigoEstudiante;

    protected PadronJpa() {
    }
}

interface PadronJpaRepo extends JpaRepository<PadronJpa, String> {
}
