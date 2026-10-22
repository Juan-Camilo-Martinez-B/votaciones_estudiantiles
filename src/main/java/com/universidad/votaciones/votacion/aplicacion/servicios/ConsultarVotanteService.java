package com.universidad.votaciones.votacion.aplicacion.servicios;

import java.util.List;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CambiarEstadoVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarVotanteUseCase;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.VotanteRepositorio;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;
import com.universidad.votaciones.votacion.dominio.Votante;

public class ConsultarVotanteService implements ConsultarVotanteUseCase, CambiarEstadoVotanteUseCase {

    private final VotanteRepositorio votantes;
    private final RegistroDeAuditoria auditoria;

    public ConsultarVotanteService(VotanteRepositorio votantes, RegistroDeAuditoria auditoria) {
        this.votantes = votantes;
        this.auditoria = auditoria;
    }

    @Override
    public Votante porId(Long id) {
        return votantes.buscarPorId(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    public Votante porCodigo(String codigoEstudiantil) {
        return votantes.buscarPorCodigo(codigoEstudiantil)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "No se encontró el votante con código estudiantil: " + codigoEstudiantil));
    }

    @Override
    public boolean estaHabilitado(String codigoEstudiantil) {
        return votantes.buscarPorCodigo(codigoEstudiantil).map(Votante::estaHabilitado).orElse(false);
    }

    @Override
    public List<Votante> listar(EstadoVotante estado) {
        return estado == null ? votantes.listar() : votantes.listarPorEstado(estado);
    }

    @Override
    public Votante cambiar(Long id, EstadoVotante estado) {
        Votante actualizado = votantes.actualizarEstado(id, estado);
        auditoria.exito(actualizado.codigoEstudiantil(), HechoElectoral.USUARIO_ACTUALIZADO,
                "Estado de votante actualizado a " + estado);
        return actualizado;
    }
}
