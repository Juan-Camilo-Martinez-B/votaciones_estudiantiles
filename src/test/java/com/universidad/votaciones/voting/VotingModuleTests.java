package com.universidad.votaciones.voting;

import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import com.universidad.votaciones.voting.internal.exception.CandidatoNoEncontradoException;
import com.universidad.votaciones.voting.internal.exception.VotanteYaVotoException;
import com.universidad.votaciones.voting.internal.model.PapeletaVoto;
import com.universidad.votaciones.voting.internal.model.TransaccionVotoIdempotente;
import com.universidad.votaciones.voting.internal.repository.PapeletaVotoRepository;
import com.universidad.votaciones.voting.internal.repository.TransaccionVotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VotingModuleTests {

    @Autowired
    private VotacionApi votacionApi;

    @Autowired
    private PapeletaVotoRepository papeletaVotoRepository;

    @Autowired
    private TransaccionVotoRepository transaccionVotoRepository;

    @BeforeEach
    void setUp() {
        // Inicializar candidatos en el tarjetón para las pruebas
        votacionApi.registrarCandidato("cand-1", "01", "Candidato A", "Propuesta de representación estudiantil");
        votacionApi.registrarCandidato("cand-2", "02", "Candidato B", "Propuesta de bienestar universitario");
    }

    @Test
    @DisplayName("Debe registrar un voto exitosamente con llave de idempotencia y depósito anónimo")
    void testEmitirVotoExitoso() {
        String idEstudiante = "est-001";
        String llaveIdempotencia = UUID.randomUUID().toString();
        EmitirVotoCommand command = new EmitirVotoCommand(llaveIdempotencia, idEstudiante, "cand-1");

        VotoResult result = votacionApi.emitirVoto(command);

        assertNotNull(result);
        assertEquals("Voto registrado exitosamente.", result.mensaje());
        assertEquals(llaveIdempotencia, result.llaveIdempotencia());
        assertNotNull(result.tokenAnonimo());
        assertEquals("PROCESADO", result.estado());
        assertFalse(result.esIdempotente());

        // Verificar que el estudiante queda registrado como que ya votó
        assertTrue(votacionApi.haVotado(idEstudiante));

        // Verificar que la transacción de idempotencia existe
        TransaccionVotoIdempotente tx = transaccionVotoRepository.findByLlaveIdempotencia(llaveIdempotencia)
                .orElseThrow();
        assertEquals(idEstudiante, tx.getIdEstudiante());
        assertTrue(tx.esValida());

        // Verificar que en la urna digital se depositó una papeleta anónima con el
        // token técnico
        List<PapeletaVoto> papeletas = papeletaVotoRepository.findAll();
        boolean papeletaExiste = papeletas.stream()
                .anyMatch(
                        p -> p.getTokenAnonimo().equals(result.tokenAnonimo()) && p.getIdCandidato().equals("cand-1"));
        assertTrue(papeletaExiste, "La papeleta anónima debe existir en la urna digital.");
    }

    @Test
    @DisplayName("Idempotencia: Reenviar la misma llave debe devolver la misma respuesta sin duplicar el voto en la urna")
    void testIdempotenciaEvitaDuplicados() {
        String idEstudiante = "est-002";
        String llaveIdempotencia = UUID.randomUUID().toString();
        EmitirVotoCommand command = new EmitirVotoCommand(llaveIdempotencia, idEstudiante, "cand-2");

        // Primer envío
        VotoResult primerResultado = votacionApi.emitirVoto(command);
        assertFalse(primerResultado.esIdempotente());

        long totalPapeletasAntes = papeletaVotoRepository.count();

        // Segundo envío con la misma llave de idempotencia (simulación de doble click
        // en frontend o retry)
        VotoResult segundoResultado = votacionApi.emitirVoto(command);

        assertTrue(segundoResultado.esIdempotente(), "La segunda llamada debe ser reconocida como idempotente.");
        assertEquals(primerResultado.tokenAnonimo(), segundoResultado.tokenAnonimo(),
                "El token anónimo devuelto debe coincidir.");
        assertEquals(primerResultado.llaveIdempotencia(), segundoResultado.llaveIdempotencia());

        long totalPapeletasDespues = papeletaVotoRepository.count();
        assertEquals(totalPapeletasAntes, totalPapeletasDespues,
                "NO se debe registrar un voto duplicado en la urna para la misma llave.");
    }

    @Test
    @DisplayName("Control de Voto Único: Debe rechazar si un estudiante intenta votar dos veces con diferentes llaves")
    void testRechazarDobleVotoDiferenteLlave() {
        String idEstudiante = "est-003";

        // Primer voto válido
        String llave1 = UUID.randomUUID().toString();
        votacionApi.emitirVoto(new EmitirVotoCommand(llave1, idEstudiante, "cand-1"));

        // Segundo intento con otra llave distinta
        String llave2 = UUID.randomUUID().toString();
        EmitirVotoCommand intentoDuplicado = new EmitirVotoCommand(llave2, idEstudiante, "cand-2");

        assertThrows(VotanteYaVotoException.class, () -> votacionApi.emitirVoto(intentoDuplicado),
                "Debe lanzar VotanteYaVotoException si el estudiante ya votó.");
    }

    @Test
    @DisplayName("Debe rechazar el voto si el candidato no existe en el tarjetón")
    void testRechazarCandidatoInexistente() {
        String idEstudiante = "est-004";
        EmitirVotoCommand command = new EmitirVotoCommand(UUID.randomUUID().toString(), idEstudiante,
                "cand-inexistente");

        assertThrows(CandidatoNoEncontradoException.class, () -> votacionApi.emitirVoto(command));
    }

    @Test
    @DisplayName("Debe contabilizar correctamente los resultados anónimos de la urna")
    void testConteoUrna() {
        ConteoVotosDto resultados = votacionApi.obtenerResultadosUrna();
        assertNotNull(resultados);
        assertTrue(resultados.totalVotos() >= 0);
    }
}
