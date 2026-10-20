package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.time.LocalDate;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.dominio.EstadoJornada;
import com.universidad.votaciones.votacion.dominio.JornadaElectoral;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class JornadaRepositoryJpa implements JornadaRepository {

    static final String ID = "actual";

    private final JornadaJpaRepo repo;

    JornadaRepositoryJpa(JornadaJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public JornadaElectoral obtener() {
        return repo.findById(ID)
                .map(fila -> JornadaElectoral.reconstruir(fila.estado, fila.dia))
                .orElseGet(JornadaElectoral::nueva);
    }

    @Override
    public void guardar(JornadaElectoral jornada) {
        JornadaJpa fila = repo.findById(ID).orElseGet(JornadaJpa::new);
        fila.id = ID;
        fila.estado = jornada.estado();
        fila.dia = jornada.dia();
        repo.save(fila);
    }
}

@Entity
@Table(name = "jornada_electoral")
class JornadaJpa {
    @Id
    String id;
    @Enumerated(EnumType.STRING)
    EstadoJornada estado;
    LocalDate dia;

    protected JornadaJpa() {
    }
}

interface JornadaJpaRepo extends JpaRepository<JornadaJpa, String> {
}
