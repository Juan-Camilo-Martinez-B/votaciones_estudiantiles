package com.universidad.votaciones.votacion.aplicacion.servicios;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CensoAcademico;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CodificadorDeClaves;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.VotanteRepositorio;
import com.universidad.votaciones.votacion.dominio.CodigoEstudiantilYaExisteException;
import com.universidad.votaciones.votacion.dominio.EmailYaExisteException;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;

public class RegistrarVotanteService implements RegistrarVotanteUseCase {

    private final VotanteRepositorio votantes;
    private final CodificadorDeClaves claves;
    private final CensoAcademico censo;
    private final RegistroDeAuditoria auditoria;

    public RegistrarVotanteService(VotanteRepositorio votantes, CodificadorDeClaves claves,
                                   CensoAcademico censo, RegistroDeAuditoria auditoria) {
        this.votantes = votantes;
        this.claves = claves;
        this.censo = censo;
        this.auditoria = auditoria;
    }

    @Override
    public Votante registrar(Comando comando) {
        Votante guardado = crear(comando);
        auditoria.exito(comando.codigoEstudiantil(), HechoElectoral.USUARIO_CREADO,
                "Votante estudiante registrado: " + guardado.email());
        return guardado;
    }

    @Override
    public Votante registrarVerificadoEnCenso(Comando comando) {
        Votante guardado = crear(comando);
        if (censo.estaHabilitado(comando.codigoEstudiantil())) {
            guardado = votantes.actualizarEstado(guardado.id(), EstadoVotante.HABILITADO);
        }
        auditoria.exito(comando.codigoEstudiantil(), HechoElectoral.USUARIO_CREADO,
                "Votante registrado y verificado contra el censo académico (Estado: " + guardado.estadoVotante() + ")");
        return guardado;
    }

    private Votante crear(Comando comando) {
        if (votantes.existeEmail(comando.email())) {
            throw new EmailYaExisteException(comando.email());
        }
        if (votantes.existeCodigo(comando.codigoEstudiantil())) {
            throw new CodigoEstudiantilYaExisteException(comando.codigoEstudiantil());
        }
        Votante votante = Votante.pendiente(
                comando.nombre(), comando.apellido(), comando.email(),
                comando.codigoEstudiantil(), comando.programaAcademico());
        return votantes.guardar(votante, claves.codificar(comando.password()));
    }
}
