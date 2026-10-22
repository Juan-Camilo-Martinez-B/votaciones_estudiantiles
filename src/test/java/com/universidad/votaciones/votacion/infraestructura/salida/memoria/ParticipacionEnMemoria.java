package com.universidad.votaciones.votacion.infraestructura.salida.memoria;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.dominio.Participacion;

public class ParticipacionEnMemoria implements ParticipacionRepository {

    private final Map<String, Participacion> porLlave = new ConcurrentHashMap<>();
    private final Map<String, String> llavePorEstudiante = new ConcurrentHashMap<>();

    @Override
    public Optional<Participacion> buscarPorLlave(String llaveIdempotencia) {
        if (llaveIdempotencia == null || llaveIdempotencia.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(porLlave.get(llaveIdempotencia.trim()));
    }

    @Override
    public boolean yaVoto(String idEstudiante) {
        return idEstudiante != null && llavePorEstudiante.containsKey(idEstudiante.trim());
    }

    @Override
    public void guardar(Participacion participacion) {
        porLlave.put(participacion.llaveIdempotencia(), participacion);
        llavePorEstudiante.put(participacion.idEstudiante(), participacion.llaveIdempotencia());
    }
}
