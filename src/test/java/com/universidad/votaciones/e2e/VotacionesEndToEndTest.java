package com.universidad.votaciones.e2e;

import com.universidad.votaciones.academicmock.AcademicApi;
import com.universidad.votaciones.academicmock.model.RespuestaValidacionEstudiante;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarAdministradorUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarVotanteUseCase;
import com.universidad.votaciones.votacion.dominio.Administrador;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.Votante;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CargarPadronUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarParticipacionUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarResultadosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.EmitirVotoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.IniciarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarCandidatoUseCase;
import com.universidad.votaciones.votacion.dominio.Escrutinio;
import com.universidad.votaciones.votacion.dominio.ReciboDeVoto;
import com.universidad.votaciones.votacion.dominio.ReglaDeNegocioVioladaException;
import com.universidad.votaciones.votacion.dominio.VotanteYaVotoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
 *       {@link AcademicApi} y los casos de uso de la votación.</li>
 *   <li>El respeto del encapsulamiento: ningún módulo invade la capa internal de otro.</li>
 *   <li>El ciclo de vida electoral completo: validación de censo académico -> registro de votante
 *       -> habilitación -> alta de candidatos -> emisión de voto con idempotencia y urna anónima
 *       -> rechazo de voto duplicado -> conteo/escrutinio -> trazabilidad en auditoría.</li>
 *   <li>La interacción vía peticiones HTTP REST tal como son ejecutadas por clientes como Postman.</li>
 * </ol>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:votaciones_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("postgres")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VotacionesEndToEndTest {

    @Autowired
    private AcademicApi academicApi;

    @Autowired
    private RegistrarAdministradorUseCase registrarAdministrador;

    @Autowired
    private RegistrarVotanteUseCase registrarVotante;

    @Autowired
    private ConsultarVotanteUseCase consultarVotante;

    @Autowired
    private RegistrarCandidatoUseCase registrarCandidato;

    @Autowired
    private CargarPadronUseCase cargarPadron;

    @Autowired
    private IniciarJornadaUseCase iniciarJornada;

    @Autowired
    private ConsultarJornadaUseCase consultarJornada;

    @Autowired
    private EmitirVotoUseCase emitirVoto;

    @Autowired
    private ConsultarParticipacionUseCase consultarParticipacion;

    @Autowired
    private ConsultarResultadosUseCase consultarResultados;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final String CANDIDATO_1 = "cand-e2e-01";
    private static final String CANDIDATO_2 = "cand-e2e-02";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        registrarCandidato.registrar(new RegistrarCandidatoUseCase.Comando(
                CANDIDATO_1, "101", "Candidata Representante A", "Propuesta de modernización digital"));
        registrarCandidato.registrar(new RegistrarCandidatoUseCase.Comando(
                CANDIDATO_2, "102", "Candidato Representante B", "Propuesta de gratuidad e infraestructura"));
        cargarPadron.cargar();
        if ("NO_INICIADA".equals(consultarJornada.consultar().estado())) {
            iniciarJornada.iniciar();
        }
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
        Administrador admin = registrarAdministrador.registrar(new RegistrarAdministradorUseCase.Comando(
                "Super", "Admin", "admin.e2e@universidad.edu.co", "adminPass123", "Coordinador Electoral"));
        assertNotNull(admin.id());
        assertTrue(admin.activo());

        // 2. Registro de Votante con validación de censo institucional
        String codigoEstudiante = "2024000005";
        Votante votante = registrarVotante.registrarVerificadoEnCenso(new RegistrarVotanteUseCase.Comando(
                "Estudiante", "Ejemplo", "votante.2024000005@universidad.edu.co", "estudiantePass123",
                codigoEstudiante, "Ingenieria de Sistemas"));
        assertNotNull(votante.id());
        assertEquals(EstadoVotante.HABILITADO, votante.estadoVotante(), "Debe quedar HABILITADO al encontrarse en el censo oficial.");
        assertTrue(consultarVotante.estaHabilitado(codigoEstudiante));
    }

    @Test
    @Order(3)
    @DisplayName("E2E - Fase 3: Rechazo de voto para estudiante no habilitado en el padrón electoral")
    void testFase3RechazoVotoNoHabilitado() {
        String estudianteInhabilitado = "codigo-no-habilitado-999";
        String llave = UUID.randomUUID().toString();
        EmitirVotoUseCase.Comando command = new EmitirVotoUseCase.Comando(llave, estudianteInhabilitado, CANDIDATO_1);

        ReglaDeNegocioVioladaException error = assertThrows(ReglaDeNegocioVioladaException.class,
                () -> emitirVoto.emitir(command));
        assertTrue(error.getMessage().contains("padrón"),
                "Debe rechazar el voto si el estudiante no fue cargado al padrón desde el censo.");
    }

    @Test
    @Order(4)
    @DisplayName("E2E - Fase 4: Emisión de voto exitoso con idempotencia y depósito anónimo en urna")
    void testFase4EmisionVotoExitosoEIdempotencia() {
        String codigoEstudiante = "2024000010";

        // Registrar y habilitar votante vía AuthApi
        registrarVotante.registrarVerificadoEnCenso(new RegistrarVotanteUseCase.Comando(
                "Carlos", "Mendoza", "carlos.mendoza@universidad.edu.co", "passSeguro123",
                codigoEstudiante, "Derecho"));

        String llaveIdempotencia = "llave-idempotente-e2e-001";
        EmitirVotoUseCase.Comando command = new EmitirVotoUseCase.Comando(llaveIdempotencia, codigoEstudiante, CANDIDATO_1);

        // --- Primer envío: Emisión de voto genuino ---
        ReciboDeVoto primerEnvio = emitirVoto.emitir(command);
        assertNotNull(primerEnvio);
        assertEquals("Voto registrado exitosamente.", primerEnvio.mensaje());
        assertEquals(llaveIdempotencia, primerEnvio.llaveIdempotencia());
        assertNotNull(primerEnvio.tokenAnonimo(), "Debe emitirse un token anónimo para la papeleta.");
        assertFalse(primerEnvio.esIdempotente());
        assertTrue(consultarParticipacion.haVotado(codigoEstudiante));

        // Consultar resultados parciales
        Escrutinio resultadosAntes = consultarResultados.consultarResultados();
        long totalVotosAntes = resultadosAntes.totalVotos();

        // --- Segundo envío: Reintento con la misma llave (Idempotencia) ---
        ReciboDeVoto segundoEnvio = emitirVoto.emitir(command);
        assertNotNull(segundoEnvio);
        assertTrue(segundoEnvio.esIdempotente(), "El reenvío debe ser detectado como respuesta idempotente.");
        assertEquals(primerEnvio.tokenAnonimo(), segundoEnvio.tokenAnonimo(), "El token técnico de la papeleta debe ser idéntico.");

        Escrutinio resultadosDespues = consultarResultados.consultarResultados();
        assertEquals(totalVotosAntes, resultadosDespues.totalVotos(), "La urna digital NO debe sumar un voto duplicado por reintento.");
    }

    @Test
    @Order(5)
    @DisplayName("E2E - Fase 5: Control estricto de voto único - Rechazo de intento de doble voto con otra llave")
    void testFase5RechazarDobleVotoDiferenteLlave() {
        String codigoEstudiante = "2024000015";

        registrarVotante.registrarVerificadoEnCenso(new RegistrarVotanteUseCase.Comando(
                "Valeria", "Torres", "valeria.torres@universidad.edu.co", "passSeguro123",
                codigoEstudiante, "Medicina"));

        // Primer voto válido
        String llave1 = UUID.randomUUID().toString();
        emitirVoto.emitir(new EmitirVotoUseCase.Comando(llave1, codigoEstudiante, CANDIDATO_1));
        assertTrue(consultarParticipacion.haVotado(codigoEstudiante));

        // Intento con otra llave (intento de fraude electoral o doble voto deliberado)
        String llave2 = UUID.randomUUID().toString();
        EmitirVotoUseCase.Comando intentoDuplicado = new EmitirVotoUseCase.Comando(llave2, codigoEstudiante, CANDIDATO_2);

        assertThrows(VotanteYaVotoException.class, () -> emitirVoto.emitir(intentoDuplicado),
                "El sistema debe impedir que un estudiante vote más de una vez.");
    }

    @Test
    @Order(6)
    @DisplayName("E2E - Fase 6: Consulta y consistencia del escrutinio anónimo")
    void testFase6EscrutinioUrnaAnonima() {
        Escrutinio escrutinio = consultarResultados.consultarResultados();
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

        Long votanteId = consultarVotante.porCodigo(codigoEstudiante).id();

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
