package com.universidad.votaciones.votacion.aplicacion.servicios;

import java.util.Optional;
import java.util.UUID;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.EmitirVotoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.Reloj;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.dominio.EmisionDeVoto;
import com.universidad.votaciones.votacion.dominio.MesaDeVotacion;
import com.universidad.votaciones.votacion.dominio.Participacion;
import com.universidad.votaciones.votacion.dominio.ReciboDeVoto;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;
import com.universidad.votaciones.votacion.dominio.VotanteYaVotoException;

public class EmitirVotoService implements EmitirVotoUseCase {

    private final JornadaRepository jornadas;
    private final PadronRepository padron;
    private final ParticipacionRepository participaciones;
    private final UrnaRepository urna;
    private final CandidatoRepository candidatos;
    private final Reloj reloj;
    private final RegistroDeAuditoria auditoria;

    public EmitirVotoService(JornadaRepository jornadas,
                             PadronRepository padron,
                             ParticipacionRepository participaciones,
                             UrnaRepository urna,
                             CandidatoRepository candidatos,
                             Reloj reloj,
                             RegistroDeAuditoria auditoria) {
        this.jornadas = jornadas;
        this.padron = padron;
        this.participaciones = participaciones;
        this.urna = urna;
        this.candidatos = candidatos;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    public ReciboDeVoto emitir(Comando comando) {
        String estudiante = comando.idEstudiante();
        try {
            Optional<Participacion> mismaLlave = participaciones.buscarPorLlave(comando.llaveIdempotencia());
            EmisionDeVoto emision = MesaDeVotacion.emitir(
                    jornadas.obtener(),
                    mismaLlave,
                    padron.contiene(comando.idEstudiante()),
                    participaciones.yaVoto(comando.idEstudiante()),
                    candidatos.existe(comando.idCandidato()),
                    comando.idEstudiante(),
                    comando.idCandidato(),
                    comando.llaveIdempotencia(),
                    reloj.ahora(),
                    UUID.randomUUID().toString());
            if (!emision.esNueva()) {
                auditoria.exito(estudiante, HechoElectoral.VOTO_REINTENTO,
                        "Reintento idempotente reconocido. No se depositó otra papeleta.");
                return emision.recibo();
            }
            participaciones.guardar(emision.participacion());
            urna.depositar(emision.papeleta());
            auditoria.exito(estudiante, HechoElectoral.VOTO_REGISTRADO,
                    "Voto depositado en la urna anónima.");
            return emision.recibo();
        } catch (VotanteYaVotoException e) {
            auditoria.fallo(estudiante, HechoElectoral.VOTO_DUPLICADO, e.getMessage());
            throw e;
        } catch (ReglaDeNegocioVioladaException e) {
            auditoria.fallo(estudiante, HechoElectoral.VOTO_RECHAZADO, e.getMessage());
            throw e;
        }
    }
}
