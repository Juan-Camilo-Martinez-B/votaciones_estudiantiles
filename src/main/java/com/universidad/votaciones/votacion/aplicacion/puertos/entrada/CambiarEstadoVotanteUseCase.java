package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;

public interface CambiarEstadoVotanteUseCase {
    Votante cambiar(Long id, EstadoVotante estado);
}
