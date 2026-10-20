package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import java.util.List;

import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;

public interface ConsultarVotanteUseCase {
    Votante porId(Long id);

    Votante porCodigo(String codigoEstudiantil);

    boolean estaHabilitado(String codigoEstudiantil);

    List<Votante> listar(EstadoVotante estado);
}
