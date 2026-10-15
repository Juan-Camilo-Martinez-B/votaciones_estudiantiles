package com.universidad.votaciones.voting.internal.repository;

import com.universidad.votaciones.voting.internal.model.EstadoProcesamiento;
import com.universidad.votaciones.voting.internal.model.TransaccionVotoIdempotente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransaccionVotoRepository extends JpaRepository<TransaccionVotoIdempotente, String> {

    Optional<TransaccionVotoIdempotente> findByLlaveIdempotencia(String llaveIdempotencia);

    boolean existsByIdEstudianteAndEstado(String idEstudiante, EstadoProcesamiento estado);

    Optional<TransaccionVotoIdempotente> findFirstByIdEstudianteAndEstado(String idEstudiante, EstadoProcesamiento estado);
}
