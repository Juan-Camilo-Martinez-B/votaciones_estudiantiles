package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import com.universidad.votaciones.votacion.dominio.Administrador;

public interface RegistrarAdministradorUseCase {
    record Comando(String nombre, String apellido, String email, String password, String cargo) {
    }

    Administrador registrar(Comando comando);
}
