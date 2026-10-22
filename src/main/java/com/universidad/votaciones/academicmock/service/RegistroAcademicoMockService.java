package com.universidad.votaciones.academicmock.service;

import com.universidad.votaciones.academicmock.model.EstadoHabilitacion;
import com.universidad.votaciones.academicmock.model.EstudianteHabilitado;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistroAcademicoMockService implements RegistroAcademicoService {

    private static final Logger log = LoggerFactory.getLogger(RegistroAcademicoMockService.class);

    private static final String[] PROGRAMAS = {
            "Ingenieria de Sistemas",
            "Ingenieria Civil",
            "Ingenieria Industrial",
            "Administracion de Empresas",
            "Contaduria Publica",
            "Derecho",
            "Medicina",
            "Psicologia",
            "Comunicacion Social",
            "Arquitectura"
    };

    private static final String[] NOMBRES = {
            "Andres", "Maria", "Carlos", "Laura", "Jose", "Ana", "Pedro", "Sofia",
            "Miguel", "Valentina", "Juan", "Camila", "Luis", "Isabella", "Diego", "Daniela",
            "Santiago", "Mariana", "Sebastian", "Valeria", "Nicolas", "Natalia", "Felipe", "Gabriela"
    };

    private static final String[] APELLIDOS = {
            "Gonzalez", "Rodriguez", "Martinez", "Garcia", "Lopez", "Hernandez",
            "Perez", "Sanchez", "Ramirez", "Torres", "Flores", "Rivera",
            "Gomez", "Diaz", "Castro", "Ruiz", "Jimenez", "Mendoza"
    };

    private final Map<String, EstudianteHabilitado> estudiantes = new ConcurrentHashMap<>();

    @Value("${academic-mock.cantidad-estudiantes:12000}")
    private int cantidadEstudiantes;

    @Value("${academic-mock.prefijo-codigo:2024}")
    private String prefijoCodigo;

    @Value("${academic-mock.latencia-ms:0}")
    private long latenciaMs;

    @PostConstruct
    public void cargarEstudiantesHabilitados() {
        log.info("Cargando lista oficial de {} estudiantes habilitados en el registro academico mock...", cantidadEstudiantes);
        long inicio = System.currentTimeMillis();

        for (int i = 1; i <= cantidadEstudiantes; i++) {
            String codigo = generarCodigoEstudiante(i);
            EstudianteHabilitado estudiante = new EstudianteHabilitado(
                    codigo,
                    generarNombreCompleto(i),
                    PROGRAMAS[i % PROGRAMAS.length],
                    (i % 10) + 1,
                    EstadoHabilitacion.HABILITADO
            );
            estudiantes.put(codigo, estudiante);
        }

        long duracion = System.currentTimeMillis() - inicio;
        log.info("Carga completada: {} estudiantes habilitados en memoria ({} ms)", estudiantes.size(), duracion);
    }

    private String generarCodigoEstudiante(int indice) {
        return prefijoCodigo + String.format("%06d", indice);
    }

    private String generarNombreCompleto(int indice) {
        String nombre = NOMBRES[indice % NOMBRES.length];
        String apellido1 = APELLIDOS[(indice / NOMBRES.length) % APELLIDOS.length];
        String apellido2 = APELLIDOS[(indice + 7) % APELLIDOS.length];
        return nombre + " " + apellido1 + " " + apellido2;
    }

    @Override
    public RespuestaValidacionEstudiante validarEstudiante(String codigoEstudiante) {
        simularLatencia();

        if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
            return RespuestaValidacionEstudiante.noHabilitado(
                    codigoEstudiante,
                    "Codigo de estudiante no proporcionado"
            );
        }

        EstudianteHabilitado estudiante = estudiantes.get(codigoEstudiante);
        if (estudiante == null) {
            return RespuestaValidacionEstudiante.noHabilitado(
                    codigoEstudiante,
                    "Estudiante no encontrado en la lista oficial de habilitados"
            );
        }

        return RespuestaValidacionEstudiante.habilitado(codigoEstudiante, estudiante.estado());
    }

    @Override
    public Optional<EstudianteHabilitado> consultarEstudiante(String codigoEstudiante) {
        simularLatencia();
        if (codigoEstudiante == null || codigoEstudiante.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(estudiantes.get(codigoEstudiante));
    }

    @Override
    public long contarEstudiantesHabilitados() {
        return estudiantes.size();
    }

    @Override
    public List<EstudianteHabilitado> listarEstudiantes() {
        return List.copyOf(estudiantes.values());
    }

    private void simularLatencia() {
        if (latenciaMs > 0) {
            try {
                Thread.sleep(latenciaMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
