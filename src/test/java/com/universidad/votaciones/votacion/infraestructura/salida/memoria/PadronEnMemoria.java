package com.universidad.votaciones.votacion.infraestructura.salida.memoria;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;

public class PadronEnMemoria implements PadronRepository {

    private final Set<String> codigos = ConcurrentHashMap.newKeySet();

    @Override
    public void reemplazar(Collection<String> nuevos) {
        Set<String> limpios = new HashSet<>();
        if (nuevos != null) {
            for (String codigo : nuevos) {
                if (codigo != null && !codigo.isBlank()) {
                    limpios.add(codigo.trim());
                }
            }
        }
        synchronized (codigos) {
            codigos.clear();
            codigos.addAll(limpios);
        }
    }

    @Override
    public boolean contiene(String codigoEstudiante) {
        return codigoEstudiante != null && codigos.contains(codigoEstudiante.trim());
    }

    @Override
    public int cantidad() {
        return codigos.size();
    }
}
