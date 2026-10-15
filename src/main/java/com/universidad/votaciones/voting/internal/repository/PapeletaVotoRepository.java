package com.universidad.votaciones.voting.internal.repository;

import com.universidad.votaciones.voting.internal.model.PapeletaVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PapeletaVotoRepository extends JpaRepository<PapeletaVoto, String> {

    boolean existsByTokenAnonimo(String tokenAnonimo);

    long countByIdCandidato(String idCandidato);

    @Query("SELECT p.idCandidato, COUNT(p) FROM PapeletaVoto p GROUP BY p.idCandidato")
    List<Object[]> contarVotosPorCandidato();
}
