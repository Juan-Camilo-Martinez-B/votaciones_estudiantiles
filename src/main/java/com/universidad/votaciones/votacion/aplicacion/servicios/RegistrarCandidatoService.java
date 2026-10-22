package com.universidad.votaciones.votacion.aplicacion.servicios;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarCandidatoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.dominio.Candidato;

public class RegistrarCandidatoService implements RegistrarCandidatoUseCase {

    private final CandidatoRepository candidatos;
    private final RegistroDeAuditoria auditoria;

    public RegistrarCandidatoService(CandidatoRepository candidatos, RegistroDeAuditoria auditoria) {
        this.candidatos = candidatos;
        this.auditoria = auditoria;
    }

    @Override
    public Candidato registrar(Comando comando) {
        Candidato candidato = new Candidato(
                comando.idCandidato(),
                comando.numeroTarjeton(),
                comando.nombreCandidato(),
                comando.propuestaResumen());
        candidatos.guardar(candidato);
        auditoria.exito("SISTEMA", HechoElectoral.CANDIDATO_REGISTRADO,
                "Candidato registrado: " + candidato.nombreCandidato() + " (#" + candidato.numeroTarjeton() + ")");
        return candidato;
    }
}
