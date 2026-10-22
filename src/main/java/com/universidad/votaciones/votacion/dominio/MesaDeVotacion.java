package com.universidad.votaciones.votacion.dominio;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Aplica, en un solo lugar, las reglas para aceptar o rechazar un voto.
 * No persiste ni habla con sistemas externos.
 */
public final class MesaDeVotacion {

    private MesaDeVotacion() {
    }

    public static EmisionDeVoto emitir(JornadaElectoral jornada,
                                       Optional<Participacion> mismaLlave,
                                       boolean estudianteEnPadron,
                                       boolean yaVoto,
                                       boolean candidatoExiste,
                                       String idEstudiante,
                                       String idCandidato,
                                       String llaveIdempotencia,
                                       LocalDateTime ahora,
                                       String tokenRecibo) {
        if (mismaLlave.isPresent()) {
            return EmisionDeVoto.idempotente(mismaLlave.get());
        }
        if (llaveIdempotencia == null || llaveIdempotencia.isBlank()) {
            throw new ReglaDeNegocioVioladaException("La llave de idempotencia es obligatoria.");
        }
        IntencionDeVoto intencion = new IntencionDeVoto(idEstudiante, idCandidato);
        jornada.asegurarPuedeRecibirVotos(ahora);
        if (!estudianteEnPadron) {
            throw new ReglaDeNegocioVioladaException(
                    "El estudiante '" + intencion.idEstudiante()
                            + "' no está en el padrón cargado desde el censo.");
        }
        if (yaVoto) {
            throw new VotanteYaVotoException(intencion.idEstudiante());
        }
        if (!candidatoExiste) {
            throw new ReglaDeNegocioVioladaException(
                    "El candidato '" + intencion.idCandidato() + "' no existe en el tarjetón.");
        }
        Papeleta papeleta = intencion.privatizar(ahora);
        Participacion participacion = Participacion.registrar(
                llaveIdempotencia, intencion.idEstudiante(), tokenRecibo, ahora);
        return EmisionDeVoto.nueva(participacion, papeleta);
    }
}
