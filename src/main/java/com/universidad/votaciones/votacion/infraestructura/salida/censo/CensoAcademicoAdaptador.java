package com.universidad.votaciones.votacion.infraestructura.salida.censo;

import java.util.List;

import org.springframework.stereotype.Component;

import com.universidad.votaciones.academicmock.AcademicApi;
import com.universidad.votaciones.academicmock.model.EstadoHabilitacion;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CensoAcademico;

@Component
public class CensoAcademicoAdaptador implements CensoAcademico {

    private final AcademicApi censo;

    public CensoAcademicoAdaptador(AcademicApi censo) {
        this.censo = censo;
    }

    @Override
    public List<String> listarCodigosHabilitados() {
        return censo.listarEstudiantesHabilitados().stream()
                .filter(estudiante -> estudiante.estado() == EstadoHabilitacion.HABILITADO)
                .map(estudiante -> estudiante.codigoEstudiante())
                .toList();
    }

    @Override
    public boolean estaHabilitado(String codigoEstudiante) {
        return censo.esEstudianteHabilitado(codigoEstudiante);
    }
}
