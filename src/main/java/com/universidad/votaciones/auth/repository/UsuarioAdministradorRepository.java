package com.universidad.votaciones.auth.repository;

import com.universidad.votaciones.auth.model.UsuarioAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioAdministradorRepository extends JpaRepository<UsuarioAdministrador, Long> {

    Optional<UsuarioAdministrador> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UsuarioAdministrador> findByActivoTrue();
}
