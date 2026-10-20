package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.List;
import java.util.Optional;

import com.universidad.votaciones.votacion.dominio.Administrador;

public interface AdministradorRepositorio {
    Administrador guardar(Administrador administrador, String claveCodificada);

    Optional<Administrador> buscarPorId(Long id);

    boolean existeEmail(String email);

    List<Administrador> listar();

    List<Administrador> listarActivos();

    Administrador actualizarActivo(Long id, boolean activo);
}
