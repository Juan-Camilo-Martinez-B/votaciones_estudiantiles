package com.universidad.votaciones.votacion.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.EmitirVotoUseCase;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.Reloj;
import com.universidad.votaciones.votacion.aplicacion.servicios.CerrarJornadaService;
import com.universidad.votaciones.votacion.aplicacion.servicios.EmitirVotoService;
import com.universidad.votaciones.votacion.aplicacion.servicios.IniciarJornadaService;
import com.universidad.votaciones.votacion.dominio.Candidato;
import com.universidad.votaciones.votacion.dominio.ReciboDeVoto;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;
import com.universidad.votaciones.votacion.dominio.VotanteYaVotoException;
import com.universidad.votaciones.votacion.infraestructura.salida.memoria.CandidatosEnMemoria;
import com.universidad.votaciones.votacion.infraestructura.salida.memoria.JornadaEnMemoria;
import com.universidad.votaciones.votacion.infraestructura.salida.memoria.PadronEnMemoria;
import com.universidad.votaciones.votacion.infraestructura.salida.memoria.ParticipacionEnMemoria;
import com.universidad.votaciones.votacion.infraestructura.salida.memoria.UrnaEnMemoria;

class EmitirVotoServiceTest {

    private static final LocalDateTime EN_HORARIO = LocalDateTime.of(2026, 10, 19, 10, 0);

    private final JornadaEnMemoria jornadas = new JornadaEnMemoria();
    private final PadronEnMemoria padron = new PadronEnMemoria();
    private final ParticipacionEnMemoria participaciones = new ParticipacionEnMemoria();
    private final UrnaEnMemoria urna = new UrnaEnMemoria();
    private final CandidatosEnMemoria candidatos = new CandidatosEnMemoria();
    private final List<String> auditoria = new ArrayList<>();
    private final Reloj reloj = () -> EN_HORARIO;

    private EmitirVotoService emitir;

    @BeforeEach
    void setUp() {
        RegistroDeAuditoria bitacora = bitacora();
        new IniciarJornadaService(jornadas, reloj, bitacora).iniciar();
        padron.reemplazar(List.of("est-1", "est-2"));
        candidatos.guardar(new Candidato("cand-1", "01", "Candidato A", "Propuesta"));
        emitir = servicio(reloj);
    }

    @Test
    @DisplayName("Un estudiante del padrón vota una vez y la urna no guarda su identidad")
    void votoUnicoYAnonimo() {
        ReciboDeVoto recibo = emitir.emitir(comando("llave-1", "est-1", "cand-1"));

        assertFalse(recibo.esIdempotente());
        assertEquals("PROCESADO", recibo.estado());
        assertEquals(1, urna.total());
        assertTrue(participaciones.yaVoto("est-1"));
        for (String detalle : auditoria) {
            assertFalse(detalle.contains("cand-1"));
        }
    }

    @Test
    @DisplayName("La misma llave devuelve el mismo recibo y no deposita otra papeleta")
    void idempotencia() {
        EmitirVotoUseCase.Comando comando = comando("llave-1", "est-1", "cand-1");
        ReciboDeVoto primero = emitir.emitir(comando);
        ReciboDeVoto segundo = emitir.emitir(comando);

        assertTrue(segundo.esIdempotente());
        assertEquals(primero.tokenAnonimo(), segundo.tokenAnonimo());
        assertEquals(1, urna.total());
    }

    @Test
    @DisplayName("Otra llave del mismo estudiante es un segundo voto y se rechaza")
    void rechazaDobleVoto() {
        emitir.emitir(comando("llave-1", "est-1", "cand-1"));

        assertThrows(VotanteYaVotoException.class,
                () -> emitir.emitir(comando("llave-2", "est-1", "cand-1")));
        assertEquals(1, urna.total());
    }

    @Test
    @DisplayName("Quien no fue cargado al padrón no puede votar")
    void exigePadronCargado() {
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> emitir.emitir(comando("llave-x", "est-desconocido", "cand-1")));
        assertEquals(0, urna.total());
    }

    @Test
    @DisplayName("Fuera del horario, con la jornada cerrada o sin iniciar, no hay voto")
    void respetaLaJornada() {
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio(() -> LocalDateTime.of(2026, 10, 19, 16, 1))
                        .emitir(comando("llave-tarde", "est-2", "cand-1")));
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> servicio(() -> LocalDateTime.of(2026, 10, 20, 10, 0))
                        .emitir(comando("llave-otro-dia", "est-2", "cand-1")));

        new CerrarJornadaService(jornadas, bitacora()).cerrar();
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> emitir.emitir(comando("llave-cerrada", "est-2", "cand-1")));
        assertEquals(0, urna.total());
    }

    @Test
    void rechazaCandidatoInexistente() {
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> emitir.emitir(comando("llave-1", "est-1", "nadie")));
    }

    private EmitirVotoService servicio(Reloj relojDelCaso) {
        return new EmitirVotoService(jornadas, padron, participaciones, urna, candidatos, relojDelCaso, bitacora());
    }

    private RegistroDeAuditoria bitacora() {
        return new RegistroDeAuditoria() {
            @Override
            public void exito(String actor, HechoElectoral hecho, String detalle) {
                auditoria.add(detalle);
            }

            @Override
            public void fallo(String actor, HechoElectoral hecho, String detalle) {
                auditoria.add(detalle);
            }
        };
    }

    private static EmitirVotoUseCase.Comando comando(String llave, String estudiante, String candidato) {
        return new EmitirVotoUseCase.Comando(llave, estudiante, candidato);
    }
}
