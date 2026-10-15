package com.universidad.votaciones.voting.internal.repository;

import com.universidad.votaciones.voting.internal.model.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidatoRepository extends JpaRepository<Candidato, String> {

    Optional<Candidato> findByNumeroTarjeton(String numeroTarjeton);
}
