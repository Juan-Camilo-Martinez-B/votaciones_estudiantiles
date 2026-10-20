package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.Optional;

import com.universidad.votaciones.votacion.dominio.Participacion;

public interface ParticipacionRepository {
    Optional<Participacion> buscarPorLlave(String llaveIdempotencia);

    boolean yaVoto(String idEstudiante);

    void guardar(Participacion participacion);
}
