package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.dominio.Papeleta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class UrnaRepositoryJpa implements UrnaRepository {

    private final PapeletaJpaRepo repo;

    UrnaRepositoryJpa(PapeletaJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public void depositar(Papeleta papeleta) {
        PapeletaJpa fila = new PapeletaJpa();
        fila.idPapeleta = papeleta.idPapeleta();
        fila.idCandidato = papeleta.idCandidato();
        fila.fechaHoraRegistro = papeleta.fechaHoraRegistro();
        fila.tokenAnonimo = UUID.randomUUID().toString();
        repo.save(fila);
    }

    @Override
    public long total() {
        return repo.count();
    }

    @Override
    public Map<String, Long> conteoPorCandidato() {
        Map<String, Long> conteo = new HashMap<>();
        for (Object[] fila : repo.contarVotosPorCandidato()) {
            conteo.put((String) fila[0], (Long) fila[1]);
        }
        return conteo;
    }
}

@Entity
@Table(name = "papeletas_voto")
class PapeletaJpa {
    @Id
    String idPapeleta;
    @Column(nullable = false)
    String idCandidato;
    /** Sello interno de la fila. No es el recibo del estudiante. */
    @Column(nullable = false, unique = true)
    String tokenAnonimo;
    @Column(nullable = false)
    LocalDateTime fechaHoraRegistro;

    protected PapeletaJpa() {
    }
}

interface PapeletaJpaRepo extends JpaRepository<PapeletaJpa, String> {

    @Query("SELECT p.idCandidato, COUNT(p) FROM PapeletaJpa p GROUP BY p.idCandidato")
    List<Object[]> contarVotosPorCandidato();
}
