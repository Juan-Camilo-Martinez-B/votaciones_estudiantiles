package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.List;

import com.universidad.votaciones.votacion.dominio.Candidato;

public interface CandidatoRepository {
    void guardar(Candidato candidato);

    boolean existe(String idCandidato);

    List<Candidato> listar();
}
