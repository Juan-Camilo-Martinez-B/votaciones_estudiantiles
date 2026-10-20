package com.universidad.votaciones.votacion.aplicacion.servicios;

import java.util.List;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CargarPadronUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CensoAcademico;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;

public class CargarPadronService implements CargarPadronUseCase {

    private final CensoAcademico censo;
    private final PadronRepository padron;
    private final RegistroDeAuditoria auditoria;

    public CargarPadronService(CensoAcademico censo, PadronRepository padron, RegistroDeAuditoria auditoria) {
        this.censo = censo;
        this.padron = padron;
        this.auditoria = auditoria;
    }

    @Override
    public int cargar() {
        List<String> codigos = censo.listarCodigosHabilitados();
        if (codigos.isEmpty()) {
            throw new ReglaDeNegocioVioladaException(
                    "El censo académico no tiene estudiantes habilitados para cargar al padrón.");
        }
        padron.reemplazar(codigos);
        auditoria.exito("SISTEMA", HechoElectoral.PADRON_CARGADO,
                "Padrón cargado con " + codigos.size() + " estudiantes habilitados.");
        return codigos.size();
    }
}
