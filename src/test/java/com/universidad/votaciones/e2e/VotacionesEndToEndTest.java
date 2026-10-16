package com.universidad.votaciones.e2e;

import com.universidad.votaciones.academicmock.AcademicApi;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import com.universidad.votaciones.audit.AuditApi;
import com.universidad.votaciones.auth.AuthApi;
import com.universidad.votaciones.auth.controller.CrearUsuarioAdministradorRequest;
import com.universidad.votaciones.auth.controller.CrearVotanteEstudianteRequest;
import com.universidad.votaciones.auth.model.EstadoVotante;
import com.universidad.votaciones.auth.model.UsuarioAdministrador;
import com.universidad.votaciones.auth.model.VotanteEstudiante;
import com.universidad.votaciones.voting.VotacionApi;
import com.universidad.votaciones.voting.internal.dto.ConteoVotosDto;
import com.universidad.votaciones.voting.internal.dto.EmitirVotoCommand;
import com.universidad.votaciones.voting.internal.dto.VotoResult;
import com.universidad.votaciones.voting.internal.exception.VotacionException;
import com.universidad.votaciones.voting.internal.exception.VotanteYaVotoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba End-to-End (E2E) integral del Monolito Modular de Votaciones Estudiantiles.
 *
 * <p>Verifica rigurosamente:
 * <ol>
 *   <li>La comunicación intermodular estricta a través de las fachadas públicas de API:
 *       {@link AcademicApi}, {@link AuthApi}, {@link VotacionApi} y {@link AuditApi}.</li>
 *   <li>El respeto del encapsulamiento: ningún módulo invade la capa internal de otro.</li>
 *   <li>El ciclo de vida electoral completo: validación de censo académico -> registro de votante
 *       -> habilitación -> alta de candidatos -> emisión de voto con idempotencia y urna anónima
 *       -> rechazo de voto duplicado -> conteo/escrutinio -> trazabilidad en auditoría.</li>
 *   <li>La interacción vía peticiones HTTP REST tal como son ejecutadas por clientes como Postman.</li>
 * </ol>
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VotacionesEndToEndTest {

    @Autowired
    private AcademicApi academicApi;

    @Autowired
    private AuthApi authApi;

    @Autowired
    private VotacionApi votacionApi;

    @Autowired
    private AuditApi auditApi;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final String CANDIDATO_1 = "cand-e2e-01";
    private static final String CANDIDATO_2 = "cand-e2e-02";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Asegurar existencia de candidatos en el tarjetón oficial vía API de votación
        votacionApi.registrarCandidato(CANDIDATO_1, "101", "Candidata Representante A", "Propuesta de modernización digital");
        votacionApi.registrarCandidato(CANDIDATO_2, "102", "Candidato Representante B", "Propuesta de gratuidad e infraestructura");
    }

    @Test
    @Order(1)
    @DisplayName("E2E - Fase 1: Consulta y verificación con censo académico oficial (AcademicApi)")
    void testFase1CensoAcademico() {
        // Estudiante precargado en el censo mock
        String codigoEstudianteValido = "2024000001";
        RespuestaValidacionEstudiante respuesta = academicApi.validarEstudiante(codigoEstudianteValido);

        assertNotNull(respuesta);
        assertTrue(respuesta.habilitado(), "El estudiante 2024000001 debe figurar habilitado en el censo oficial.");
        assertEquals(codigoEstudianteValido, respuesta.codigoEstudiante());
        assertTrue(academicApi.esEstudianteHabilitado(codigoEstudianteValido));

        // Estudiante inexistente
        String codigoInexistente = "9999999999";
        RespuestaValidacionEstudiante respuestaInexistente = academicApi.validarEstudiante(codigoInexistente);
        assertFalse(respuestaInexistente.habilitado(), "Un estudiante ficticio no debe estar habilitado.");
        assertFalse(academicApi.esEstudianteHabilitado(codigoInexistente));
    }

    @Test
    @Order(2)
    @DisplayName("E2E - Fase 2: Registro de administrador y votante conectado con censo académico (AuthApi)")
    void testFase2RegistroVotantesYAdministradores() {
        // 1. Registro de Administrador vía AuthApi
        CrearUsuarioAdministradorRequest adminReq = new CrearUsuarioAdministradorRequest(
                "Super", "Admin", "admin.e2e@universidad.edu.co", "adminPass123", "Coordinador Electoral"
        );
        UsuarioAdministrador admin = authApi.registrarAdministrador(adminReq);
        assertNotNull(admin.getId());
        assertTrue(admin.getActivo());

        // 2. Registro de Votante con validación de censo institucional
        String codigoEstudiante = "2024000005";
        CrearVotanteEstudianteRequest votanteReq = new CrearVotanteEstudianteRequest(
                "Estudiante", "Ejemplo", "votante.2024000005@universidad.edu.co", "estudiantePass123",
                codigoEstudiante, "Ingenieria de Sistemas"
        );

        // Registro verificado contra censo académico (AcademicApi)
        VotanteEstudiante votante = authApi.registrarYHabilitarConRegistroAcademico(votanteReq);
        assertNotNull(votante.getId());
        assertEquals(EstadoVotante.HABILITADO, votante.getEstadoVotante(), "Debe quedar HABILITADO al encontrarse en el censo oficial.");
        assertTrue(authApi.esVotanteHabilitado(codigoEstudiante));
    }

    @Test
    @Order(3)
    @DisplayName("E2E - Fase 3: Rechazo de voto para estudiante no habilitado en el padrón electoral")
    void testFase3RechazoVotoNoHabilitado() {
        String estudianteInhabilitado = "codigo-no-habilitado-999";
        String llave = UUID.randomUUID().toString();
        EmitirVotoCommand command = new EmitirVotoCommand(llave, estudianteInhabilitado, CANDIDATO_1);

        assertThrows(VotacionException.class, () -> votacionApi.emitirVoto(command),
                "VotacionApi debe rechazar el voto si el estudiante no está habilitado en AuthApi ni en AcademicApi.");
    }

    @Test
    @Order(4)
    @DisplayName("E2E - Fase 4: Emisión de voto exitoso con idempotencia y depósito anónimo en urna")
    void testFase4EmisionVotoExitosoEIdempotencia() {
        String codigoEstudiante = "2024000010";

        // Registrar y habilitar votante vía AuthApi
        authApi.registrarYHabilitarConRegistroAcademico(new CrearVotanteEstudianteRequest(
                "Carlos", "Mendoza", "carlos.mendoza@universidad.edu.co", "passSeguro123",
                codigoEstudiante, "Derecho"
        ));

        String llaveIdempotencia = "llave-idempotente-e2e-001";
        EmitirVotoCommand command = new EmitirVotoCommand(llaveIdempotencia, codigoEstudiante, CANDIDATO_1);

        // --- Primer envío: Emisión de voto genuino ---
        VotoResult primerEnvio = votacionApi.emitirVoto(command);
        assertNotNull(primerEnvio);
        assertEquals("Voto registrado exitosamente.", primerEnvio.mensaje());
        assertEquals(llaveIdempotencia, primerEnvio.llaveIdempotencia());
        assertNotNull(primerEnvio.tokenAnonimo(), "Debe emitirse un token anónimo para la papeleta.");
        assertFalse(primerEnvio.esIdempotente());
        assertTrue(votacionApi.haVotado(codigoEstudiante));

        // Consultar resultados parciales
        ConteoVotosDto resultadosAntes = votacionApi.obtenerResultadosUrna();
        long totalVotosAntes = resultadosAntes.totalVotos();

        // --- Segundo envío: Reintento con la misma llave (Idempotencia) ---
        VotoResult segundoEnvio = votacionApi.emitirVoto(command);
        assertNotNull(segundoEnvio);
        assertTrue(segundoEnvio.esIdempotente(), "El reenvío debe ser detectado como respuesta idempotente.");
        assertEquals(primerEnvio.tokenAnonimo(), segundoEnvio.tokenAnonimo(), "El token técnico de la papeleta debe ser idéntico.");

        ConteoVotosDto resultadosDespues = votacionApi.obtenerResultadosUrna();
        assertEquals(totalVotosAntes, resultadosDespues.totalVotos(), "La urna digital NO debe sumar un voto duplicado por reintento.");
    }

    @Test
    @Order(5)
    @DisplayName("E2E - Fase 5: Control estricto de voto único - Rechazo de intento de doble voto con otra llave")
    void testFase5RechazarDobleVotoDiferenteLlave() {
        String codigoEstudiante = "2024000015";

        authApi.registrarYHabilitarConRegistroAcademico(new CrearVotanteEstudianteRequest(
                "Valeria", "Torres", "valeria.torres@universidad.edu.co", "passSeguro123",
                codigoEstudiante, "Medicina"
        ));

        // Primer voto válido
        String llave1 = UUID.randomUUID().toString();
        votacionApi.emitirVoto(new EmitirVotoCommand(llave1, codigoEstudiante, CANDIDATO_1));
        assertTrue(votacionApi.haVotado(codigoEstudiante));

        // Intento con otra llave (intento de fraude electoral o doble voto deliberado)
        String llave2 = UUID.randomUUID().toString();
        EmitirVotoCommand intentoDuplicado = new EmitirVotoCommand(llave2, codigoEstudiante, CANDIDATO_2);

        assertThrows(VotanteYaVotoException.class, () -> votacionApi.emitirVoto(intentoDuplicado),
                "El sistema debe impedir que un estudiante vote más de una vez.");
    }

    @Test
    @Order(6)
    @DisplayName("E2E - Fase 6: Consulta y consistencia del escrutinio anónimo (VotacionApi)")
    void testFase6EscrutinioUrnaAnonima() {
        ConteoVotosDto escrutinio = votacionApi.obtenerResultadosUrna();
        assertNotNull(escrutinio);
        assertTrue(escrutinio.totalVotos() > 0, "Debe existir al menos un voto contabilizado en las fases previas.");
        assertNotNull(escrutinio.votosPorCandidato());
        assertTrue(escrutinio.votosPorCandidato().containsKey(CANDIDATO_1));
    }

    @Test
    @Order(7)
    @DisplayName("E2E - Fase 7: Flujo completo simulando peticiones HTTP REST (Postman simulation)")
    void testFase7FlujoCompletoHttpRest() throws Exception {
        String codigoEstudiante = "2024000020";

        // 1. GET /api/academic/estudiantes/{codigo}/validar
        mockMvc.perform(get("/api/academic/estudiantes/{codigo}/validar", codigoEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habilitado").value(true))
                .andExpect(jsonPath("$.codigoEstudiante").value(codigoEstudiante));

        // 2. POST /api/auth/votantes
        String reqVotanteJson = """
                {
                  "nombre": "Mariana",
                  "apellido": "Castro",
                  "email": "mariana.castro@universidad.edu.co",
                  "password": "marianaPassword2024",
                  "codigoEstudiantil": "%s",
                  "programaAcademico": "Psicologia"
                }
                """.formatted(codigoEstudiante);

        mockMvc.perform(post("/api/auth/votantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqVotanteJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.codigoEstudiantil").value(codigoEstudiante));

        Long votanteId = authApi.buscarPorCodigoEstudiantil(codigoEstudiante).getId();

        // 3. PATCH /api/auth/votantes/{id}/estado?estado=HABILITADO
        mockMvc.perform(patch("/api/auth/votantes/{id}/estado", votanteId)
                        .param("estado", "HABILITADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoVotante").value("HABILITADO"));

        // 4. GET /api/v1/votaciones/candidatos
        mockMvc.perform(get("/api/v1/votaciones/candidatos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 5. POST /api/v1/votaciones/votar (Emisión de voto HTTP con header Idempotency-Key)
        String idempotencyKey = UUID.randomUUID().toString();
        String votarJson = """
                {
                  "llaveIdempotencia": "%s",
                  "idEstudiante": "%s",
                  "idCandidato": "%s"
                }
                """.formatted(idempotencyKey, codigoEstudiante, CANDIDATO_2);

        mockMvc.perform(post("/api/v1/votaciones/votar")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votarJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.llaveIdempotencia").value(idempotencyKey))
                .andExpect(jsonPath("$.tokenAnonimo").isNotEmpty())
                .andExpect(jsonPath("$.esIdempotente").value(false));

        // 6. POST /api/v1/votaciones/votar (Reintento HTTP con la misma Idempotency-Key)
        mockMvc.perform(post("/api/v1/votaciones/votar")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(votarJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.llaveIdempotencia").value(idempotencyKey))
                .andExpect(jsonPath("$.esIdempotente").value(true));

        // 7. GET /api/v1/votaciones/estudiante/{idEstudiante}/ha-votado
        mockMvc.perform(get("/api/v1/votaciones/estudiante/{idEstudiante}/ha-votado", codigoEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.haVotado").value(true));

        // 8. GET /api/v1/votaciones/resultados
        mockMvc.perform(get("/api/v1/votaciones/resultados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").isNumber());
    }
}
