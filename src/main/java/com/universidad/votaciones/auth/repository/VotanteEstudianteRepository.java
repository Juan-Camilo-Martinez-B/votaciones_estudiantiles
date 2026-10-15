package com.universidad.votaciones.auth.repository;

import com.universidad.votaciones.auth.model.EstadoVotante;
import com.universidad.votaciones.auth.model.VotanteEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotanteEstudianteRepository extends JpaRepository<VotanteEstudiante, Long> {

    Optional<VotanteEstudiante> findByCodigoEstudiantil(String codigoEstudiantil);

    Optional<VotanteEstudiante> findByEmail(String email);

    boolean existsByCodigoEstudiantil(String codigoEstudiantil);

    boolean existsByEmail(String email);

    List<VotanteEstudiante> findByEstadoVotante(EstadoVotante estadoVotante);
}
