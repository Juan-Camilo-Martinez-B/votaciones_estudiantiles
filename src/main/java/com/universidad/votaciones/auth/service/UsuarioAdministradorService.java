package com.universidad.votaciones.auth.service;

import com.universidad.votaciones.auth.controller.CrearUsuarioAdministradorRequest;
import com.universidad.votaciones.auth.model.UsuarioAdministrador;
import com.universidad.votaciones.auth.repository.UsuarioAdministradorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioAdministradorService {

    private final UsuarioAdministradorRepository administradorRepository;
    private final UsuarioService usuarioService;

    public UsuarioAdministradorService(UsuarioAdministradorRepository administradorRepository,
                                       UsuarioService usuarioService) {
        this.administradorRepository = administradorRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public UsuarioAdministrador crear(CrearUsuarioAdministradorRequest request) {
        if (administradorRepository.existsByEmail(request.email())) {
            throw new EmailYaExisteException(request.email());
        }

        UsuarioAdministrador administrador = new UsuarioAdministrador(
                request.nombre(),
                request.apellido(),
                request.email(),
                usuarioService.codificarPassword(request.password()),
                request.cargo(),
                true
        );

        return administradorRepository.save(administrador);
    }

    @Transactional(readOnly = true)
    public UsuarioAdministrador buscarPorId(Long id) {
        return administradorRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdministrador> listarTodos() {
        return administradorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdministrador> listarActivos() {
        return administradorRepository.findByActivoTrue();
    }

    @Transactional
    public UsuarioAdministrador activar(Long id) {
        UsuarioAdministrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        administrador.setActivo(true);
        return administradorRepository.save(administrador);
    }

    @Transactional
    public UsuarioAdministrador desactivar(Long id) {
        UsuarioAdministrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        administrador.setActivo(false);
        return administradorRepository.save(administrador);
    }
}
