package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.Candidato;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class CandidatoRepositoryJpa implements CandidatoRepository {

    private final CandidatoJpaRepo repo;

    CandidatoRepositoryJpa(CandidatoJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public void guardar(Candidato candidato) {
        CandidatoJpa fila = repo.findById(candidato.idCandidato()).orElseGet(CandidatoJpa::new);
        fila.idCandidato = candidato.idCandidato();
        fila.numeroTarjeton = candidato.numeroTarjeton();
        fila.nombreCandidato = candidato.nombreCandidato();
        fila.propuestaResumen = candidato.propuestaResumen();
        repo.save(fila);
    }

    @Override
    public boolean existe(String idCandidato) {
        return idCandidato != null && !idCandidato.isBlank() && repo.existsById(idCandidato.trim());
    }

    @Override
    public List<Candidato> listar() {
        return repo.findAll().stream()
                .map(fila -> new Candidato(fila.idCandidato, fila.numeroTarjeton, fila.nombreCandidato, fila.propuestaResumen))
                .toList();
    }
}

@Entity
@Table(name = "candidatos")
class CandidatoJpa {
    @Id
    String idCandidato;
    @Column(nullable = false, unique = true)
    String numeroTarjeton;
    @Column(nullable = false)
    String nombreCandidato;
    @Column(length = 1000)
    String propuestaResumen;

    protected CandidatoJpa() {
    }
}

interface CandidatoJpaRepo extends JpaRepository<CandidatoJpa, String> {
}
