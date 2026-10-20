package com.universidad.votaciones.votacion.aplicacion.servicios;

import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.ConsultarAdministradorUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.entrada.RegistrarAdministradorUseCase;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.AdministradorRepositorio;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CodificadorDeClaves;
import com.universidad.votaciones.votacion.dominio.HechoElectoral;
import com.universidad.votaciones.votacion.aplicacion.puertos.salida.RegistroDeAuditoria;
import com.universidad.votaciones.votacion.dominio.Administrador;
import com.universidad.votaciones.votacion.dominio.EmailYaExisteException;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;

import java.util.List;

public class RegistrarAdministradorService implements RegistrarAdministradorUseCase, ConsultarAdministradorUseCase {

    private final AdministradorRepositorio administradores;
    private final CodificadorDeClaves claves;
    private final RegistroDeAuditoria auditoria;

    public RegistrarAdministradorService(AdministradorRepositorio administradores, CodificadorDeClaves claves,
                                         RegistroDeAuditoria auditoria) {
        this.administradores = administradores;
        this.claves = claves;
        this.auditoria = auditoria;
    }

    @Override
    public Administrador registrar(Comando comando) {
        if (administradores.existeEmail(comando.email())) {
            throw new EmailYaExisteException(comando.email());
        }
        Administrador administrador = Administrador.nuevo(
                comando.nombre(), comando.apellido(), comando.email(), comando.cargo());
        Administrador guardado = administradores.guardar(administrador, claves.codificar(comando.password()));
        auditoria.exito(guardado.email(), HechoElectoral.USUARIO_CREADO,
                "Usuario administrador creado: " + guardado.email());
        return guardado;
    }

    @Override
    public Administrador porId(Long id) {
        return administradores.buscarPorId(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    public List<Administrador> listar() {
        return administradores.listar();
    }

    @Override
    public List<Administrador> listarActivos() {
        return administradores.listarActivos();
    }

    @Override
    public Administrador cambiarActivo(Long id, boolean activo) {
        return administradores.actualizarActivo(id, activo);
    }
}
