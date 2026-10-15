package com.universidad.votaciones.auth.service;

import com.universidad.votaciones.auth.controller.CrearVotanteEstudianteRequest;
import com.universidad.votaciones.auth.model.EstadoVotante;
import com.universidad.votaciones.auth.model.VotanteEstudiante;
import com.universidad.votaciones.auth.repository.VotanteEstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VotanteEstudianteService {

    private final VotanteEstudianteRepository votanteRepository;
    private final UsuarioService usuarioService;

    public VotanteEstudianteService(VotanteEstudianteRepository votanteRepository,
                                    UsuarioService usuarioService) {
        this.votanteRepository = votanteRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public VotanteEstudiante crear(CrearVotanteEstudianteRequest request) {
        validarDatosUnicos(request.email(), request.codigoEstudiantil());

        VotanteEstudiante votante = new VotanteEstudiante(
                request.nombre(),
                request.apellido(),
                request.email(),
                usuarioService.codificarPassword(request.password()),
                request.codigoEstudiantil(),
                request.programaAcademico(),
                EstadoVotante.PENDIENTE_VERIFICACION
        );

        return votanteRepository.save(votante);
    }

    @Transactional(readOnly = true)
    public VotanteEstudiante buscarPorId(Long id) {
        return votanteRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public VotanteEstudiante buscarPorCodigoEstudiantil(String codigoEstudiantil) {
        return votanteRepository.findByCodigoEstudiantil(codigoEstudiantil)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "No se encontró el votante con código estudiantil: " + codigoEstudiantil));
    }

    @Transactional(readOnly = true)
    public List<VotanteEstudiante> listarTodos() {
        return votanteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<VotanteEstudiante> listarPorEstado(EstadoVotante estado) {
        return votanteRepository.findByEstadoVotante(estado);
    }

    @Transactional(readOnly = true)
    public boolean esVotanteHabilitado(String codigoEstudiantil) {
        return votanteRepository.findByCodigoEstudiantil(codigoEstudiantil)
                .map(votante -> votante.getEstadoVotante() == EstadoVotante.HABILITADO)
                .orElse(false);
    }

    @Transactional
    public VotanteEstudiante actualizarEstado(Long id, EstadoVotante nuevoEstado) {
        VotanteEstudiante votante = votanteRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        votante.setEstadoVotante(nuevoEstado);
        return votanteRepository.save(votante);
    }

    private void validarDatosUnicos(String email, String codigoEstudiantil) {
        if (votanteRepository.existsByEmail(email)) {
            throw new EmailYaExisteException(email);
        }
        if (votanteRepository.existsByCodigoEstudiantil(codigoEstudiantil)) {
            throw new CodigoEstudiantilYaExisteException(codigoEstudiantil);
        }
    }
}
