package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import com.universidad.votaciones.votacion.dominio.Candidato;

public interface RegistrarCandidatoUseCase {
    record Comando(String idCandidato, String numeroTarjeton, String nombreCandidato, String propuestaResumen) {
    }

    Candidato registrar(Comando comando);
}
