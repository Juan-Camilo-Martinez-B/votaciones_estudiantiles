package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import java.util.List;

import com.universidad.votaciones.votacion.dominio.Candidato;

public interface ConsultarCandidatosUseCase {
    List<Candidato> listar();
}
