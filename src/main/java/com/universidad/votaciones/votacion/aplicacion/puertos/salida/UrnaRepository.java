package com.universidad.votaciones.votacion.aplicacion.puertos.salida;

import java.util.Map;

import com.universidad.votaciones.votacion.dominio.Papeleta;

public interface UrnaRepository {
    void depositar(Papeleta papeleta);

    long total();

    Map<String, Long> conteoPorCandidato();
}
