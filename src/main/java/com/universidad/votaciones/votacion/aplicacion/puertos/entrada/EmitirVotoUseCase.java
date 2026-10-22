package com.universidad.votaciones.votacion.aplicacion.puertos.entrada;

import com.universidad.votaciones.votacion.dominio.ReciboDeVoto;

public interface EmitirVotoUseCase {
    record Comando(String llaveIdempotencia, String idEstudiante, String idCandidato) {
    }

    ReciboDeVoto emitir(Comando comando);
}
