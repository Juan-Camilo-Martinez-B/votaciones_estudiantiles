package com.universidad.votaciones.votacion.aplicacion.servicios;

import java.util.List;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarCandidatosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarParticipacionUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarResultadosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.dominio.Candidato;
import com.universidad.votaciones.votacion.dominio.Escrutinio;
import com.universidad.votaciones.votacion.dominio.EstadoDeJornada;

public class ConsultarVotacionService implements ConsultarJornadaUseCase, ConsultarCandidatosUseCase,
        ConsultarResultadosUseCase, ConsultarParticipacionUseCase {

    private final JornadaRepository jornadas;
    private final CandidatoRepository candidatos;
    private final UrnaRepository urna;
    private final ParticipacionRepository participaciones;
    private final RegistroDeAuditoria auditoria;

    public ConsultarVotacionService(JornadaRepository jornadas,
                                    CandidatoRepository candidatos,
                                    UrnaRepository urna,
                                    ParticipacionRepository participaciones,
                                    RegistroDeAuditoria auditoria) {
        this.jornadas = jornadas;
        this.candidatos = candidatos;
        this.urna = urna;
        this.participaciones = participaciones;
        this.auditoria = auditoria;
    }

    @Override
    public EstadoDeJornada consultar() {
        return jornadas.obtener().describir();
    }

    @Override
    public List<Candidato> listar() {
        return candidatos.listar();
    }

    @Override
    public Escrutinio consultarResultados() {
        Escrutinio escrutinio = new Escrutinio(urna.total(), urna.conteoPorCandidato());
        auditoria.exito("SISTEMA", HechoElectoral.RESULTADOS_CONSULTADOS,
                "Consulta de escrutinio de la urna anónima. Total votos: " + escrutinio.totalVotos());
        return escrutinio;
    }

    @Override
    public boolean haVotado(String idEstudiante) {
        return idEstudiante != null && participaciones.yaVoto(idEstudiante);
    }
}
