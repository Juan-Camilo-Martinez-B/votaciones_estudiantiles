package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import java.util.List;

import com.universidad.votaciones.votacion.dominio.Administrador;

public interface ConsultarAdministradorUseCase {
    Administrador porId(Long id);

    List<Administrador> listar();

    List<Administrador> listarActivos();

    Administrador cambiarActivo(Long id, boolean activo);
}
