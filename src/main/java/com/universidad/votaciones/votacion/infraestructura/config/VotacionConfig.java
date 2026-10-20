package com.universidad.votaciones.votacion.infraestructura.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CargarPadronUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.CerrarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarCandidatosUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.EmitirVotoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.IniciarJornadaUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarAdministradorUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarCandidatoUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarVotanteUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.AdministradorRepositorio;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CandidatoRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CensoAcademico;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CodificadorDeClaves;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.JornadaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.PadronRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.ParticipacionRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.Reloj;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.UrnaRepository;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.VotanteRepositorio;
import com.universidad.votaciones.votacion.aplicacion.servicios.CargarPadronService;
import com.universidad.votaciones.votacion.aplicacion.servicios.CerrarJornadaService;
import com.universidad.votaciones.votacion.aplicacion.servicios.ConsultarVotacionService;
import com.universidad.votaciones.votacion.aplicacion.servicios.ConsultarVotanteService;
import com.universidad.votaciones.votacion.aplicacion.servicios.EmitirVotoService;
import com.universidad.votaciones.votacion.aplicacion.servicios.IniciarJornadaService;
import com.universidad.votaciones.votacion.aplicacion.servicios.RegistrarAdministradorService;
import com.universidad.votaciones.votacion.aplicacion.servicios.RegistrarCandidatoService;
import com.universidad.votaciones.votacion.aplicacion.servicios.RegistrarVotanteService;
import com.universidad.votaciones.votacion.dominio.Votante;

@Configuration
class VotacionConfig {

    @Bean
    Reloj reloj(@Value("${votacion.reloj:sistema}") String valor) {
        if (valor == null || valor.isBlank() || "sistema".equalsIgnoreCase(valor)) {
            return LocalDateTime::now;
        }
        LocalDateTime fijo = LocalDateTime.parse(valor);
        return () -> fijo;
    }

    @Bean
    IniciarJornadaUseCase iniciarJornada(JornadaRepository jornadas, Reloj reloj, RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        IniciarJornadaUseCase servicio = new IniciarJornadaService(jornadas, reloj, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return () -> tx.execute(estado -> servicio.iniciar());
    }

    @Bean
    CerrarJornadaUseCase cerrarJornada(JornadaRepository jornadas, RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        CerrarJornadaUseCase servicio = new CerrarJornadaService(jornadas, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return () -> tx.execute(estado -> servicio.cerrar());
    }

    @Bean
    CargarPadronUseCase cargarPadron(CensoAcademico censo, PadronRepository padron, RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        CargarPadronUseCase servicio = new CargarPadronService(censo, padron, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return () -> {
            Integer cantidad = tx.execute(estado -> servicio.cargar());
            return cantidad == null ? 0 : cantidad;
        };
    }

    @Bean
    EmitirVotoUseCase emitirVoto(JornadaRepository jornadas,
            PadronRepository padron,
            ParticipacionRepository participaciones,
            UrnaRepository urna,
            CandidatoRepository candidatos,
            Reloj reloj,
            RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        EmitirVotoUseCase servicio = new EmitirVotoService(
                jornadas, padron, participaciones, urna, candidatos, reloj, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return comando -> tx.execute(estado -> servicio.emitir(comando));
    }

    @Bean
    RegistrarCandidatoUseCase registrarCandidato(CandidatoRepository candidatos, RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        RegistrarCandidatoUseCase servicio = new RegistrarCandidatoService(candidatos, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return comando -> tx.execute(estado -> servicio.registrar(comando));
    }

    @Bean
    RegistrarVotanteUseCase registrarVotante(VotanteRepositorio votantes, CodificadorDeClaves claves,
            CensoAcademico censo, RegistroDeAuditoria auditoria,
            ObjectProvider<PlatformTransactionManager> transacciones) {
        RegistrarVotanteService servicio = new RegistrarVotanteService(votantes, claves, censo, auditoria);
        TransactionTemplate tx = plantilla(transacciones);
        if (tx == null) {
            return servicio;
        }
        return new RegistrarVotanteUseCase() {
            @Override
            public Votante registrar(Comando comando) {
                return tx.execute(estado -> servicio.registrar(comando));
            }

            @Override
            public Votante registrarVerificadoEnCenso(Comando comando) {
                return tx.execute(estado -> servicio.registrarVerificadoEnCenso(comando));
            }
        };
    }

    @Bean
    ConsultarVotanteService consultarVotante(VotanteRepositorio votantes, RegistroDeAuditoria auditoria) {
        return new ConsultarVotanteService(votantes, auditoria);
    }

    @Bean
    RegistrarAdministradorService registrarAdministrador(AdministradorRepositorio administradores,
            CodificadorDeClaves claves, RegistroDeAuditoria auditoria) {
        return new RegistrarAdministradorService(administradores, claves, auditoria);
    }

    @Bean
    ConsultarVotacionService consultas(JornadaRepository jornadas,
            CandidatoRepository candidatos,
            UrnaRepository urna,
            ParticipacionRepository participaciones,
            RegistroDeAuditoria auditoria) {
        return new ConsultarVotacionService(jornadas, candidatos, urna, participaciones, auditoria);
    }

    @Bean
    CommandLineRunner tarjetonInicial(ConsultarCandidatosUseCase consultas, RegistrarCandidatoUseCase registrar) {
        return args -> {
            if (!consultas.listar().isEmpty()) {
                return;
            }
            registrar.registrar(new RegistrarCandidatoUseCase.Comando(
                    "candidato-1", "01", "Carlos Mendoza",
                    "Modernización tecnológica y bienestar estudiantil"));
            registrar.registrar(new RegistrarCandidatoUseCase.Comando(
                    "candidato-2", "02", "Laura Restrepo",
                    "Transparencia y participación activa universitaria"));
            registrar.registrar(new RegistrarCandidatoUseCase.Comando(
                    "candidato-blanco", "99", "Voto en Blanco",
                    "Opción democrática institucional"));
        };
    }

    private static TransactionTemplate plantilla(ObjectProvider<PlatformTransactionManager> transacciones) {
        PlatformTransactionManager tm = transacciones.getIfAvailable();
        return tm == null ? null : new TransactionTemplate(tm);
    }
}
