package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import com.universidad.votaciones.votacion.dominio.Votante;

public interface RegistrarVotanteUseCase {
    record Comando(String nombre, String apellido, String email, String password,
                   String codigoEstudiantil, String programaAcademico) {
    }

    Votante registrar(Comando comando);

    Votante registrarVerificadoEnCenso(Comando comando);
}
