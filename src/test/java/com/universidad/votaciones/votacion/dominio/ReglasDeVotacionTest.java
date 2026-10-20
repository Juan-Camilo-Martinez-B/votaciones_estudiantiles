package com.universidad.votaciones.votacion.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReglasDeVotacionTest {

    private static final LocalDateTime EN_HORARIO = LocalDateTime.of(2026, 10, 19, 10, 0);

    @Test
    @DisplayName("La jornada solo abre entre las 08:00 y las 16:00 y no se reabre")
    void jornadaDeUnSoloDia() {
        JornadaElectoral jornada = JornadaElectoral.nueva();
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> jornada.iniciar(LocalDateTime.of(2026, 10, 19, 7, 59)));

        jornada.iniciar(EN_HORARIO);
        assertEquals(EstadoJornada.ABIERTA, jornada.estado());
        jornada.asegurarPuedeRecibirVotos(LocalDateTime.of(2026, 10, 19, 8, 0));
        jornada.asegurarPuedeRecibirVotos(LocalDateTime.of(2026, 10, 19, 16, 0));

        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> jornada.asegurarPuedeRecibirVotos(LocalDateTime.of(2026, 10, 19, 16, 0, 1)));
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> jornada.asegurarPuedeRecibirVotos(LocalDateTime.of(2026, 10, 20, 10, 0)));
        assertThrows(ReglaDeNegocioVioladaException.class, () -> jornada.iniciar(EN_HORARIO));

        jornada.cerrar();
        assertEquals(EstadoJornada.CERRADA, jornada.estado());
        assertThrows(ReglaDeNegocioVioladaException.class, () -> jornada.iniciar(EN_HORARIO));
        assertThrows(ReglaDeNegocioVioladaException.class,
                () -> jornada.asegurarPuedeRecibirVotos(EN_HORARIO));
    }

    @Test
    @DisplayName("Privatizar corta el vínculo entre el estudiante y el candidato")
    void privatizarNoConservaAlEstudiante() {
        IntencionDeVoto intencion = new IntencionDeVoto("2024000001", "cand-1");
        Papeleta papeleta = intencion.privatizar(EN_HORARIO);
        Participacion participacion = Participacion.registrar("llave-1", "2024000001", "recibo-1", EN_HORARIO);

        assertEquals("cand-1", papeleta.idCandidato());
        assertFalse(nombresDeCampos(Papeleta.class).stream().anyMatch(nombre -> nombre.toLowerCase().contains("estudiante")));
        assertFalse(nombresDeCampos(Papeleta.class).stream().anyMatch(nombre -> nombre.toLowerCase().contains("recibo")));
        assertFalse(nombresDeCampos(Papeleta.class).stream().anyMatch(nombre -> nombre.toLowerCase().contains("llave")));
        assertFalse(nombresDeCampos(Participacion.class).stream().anyMatch(nombre -> nombre.toLowerCase().contains("candidato")));
        assertFalse(papeleta.toString().contains("2024000001"));
        assertTrue(participacion.idEstudiante().equals("2024000001"));
    }

    private static java.util.List<String> nombresDeCampos(Class<?> tipo) {
        return Arrays.stream(tipo.getDeclaredFields()).map(Field::getName).toList();
    }
}
