package com.universidad.votaciones.votacion.infraestructura.salida.memoria;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.Candidato;

public class CandidatosEnMemoria implements CandidatoRepository {

    private final Map<String, Candidato> datos = new ConcurrentHashMap<>();

    @Override
    public void guardar(Candidato candidato) {
        datos.put(candidato.idCandidato(), candidato);
    }

    @Override
    public boolean existe(String idCandidato) {
        return idCandidato != null && datos.containsKey(idCandidato.trim());
    }

    @Override
    public List<Candidato> listar() {
        return List.copyOf(datos.values());
    }
}
