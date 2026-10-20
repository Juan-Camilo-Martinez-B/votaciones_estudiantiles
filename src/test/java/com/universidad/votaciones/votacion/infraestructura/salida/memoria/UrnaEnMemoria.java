package com.universidad.votaciones.votacion.infraestructura.salida.memoria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.dominio.Papeleta;

public class UrnaEnMemoria implements UrnaRepository {

    private final List<Papeleta> papeletas = new CopyOnWriteArrayList<>();

    @Override
    public void depositar(Papeleta papeleta) {
        papeletas.add(papeleta);
    }

    @Override
    public long total() {
        return papeletas.size();
    }

    @Override
    public Map<String, Long> conteoPorCandidato() {
        Map<String, Long> conteo = new HashMap<>();
        for (Papeleta papeleta : papeletas) {
            conteo.merge(papeleta.idCandidato(), 1L, Long::sum);
        }
        return Map.copyOf(conteo);
    }
}
