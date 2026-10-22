package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.List;
import java.util.Optional;

import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;

public interface VotanteRepositorio {
    Votante guardar(Votante votante, String claveCodificada);

    Optional<Votante> buscarPorId(Long id);

    Optional<Votante> buscarPorCodigo(String codigoEstudiantil);

    boolean existeEmail(String email);

    boolean existeCodigo(String codigoEstudiantil);

    List<Votante> listar();

    List<Votante> listarPorEstado(EstadoVotante estado);

    Votante actualizarEstado(Long id, EstadoVotante estado);
}
