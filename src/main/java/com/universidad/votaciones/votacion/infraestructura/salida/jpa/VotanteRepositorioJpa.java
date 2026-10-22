package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.VotanteRepositorio;
import com.universidad.votaciones.votacion.dominio.EstadoVotante;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;
import com.universidad.votaciones.votacion.dominio.Votante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class VotanteRepositorioJpa implements VotanteRepositorio {

    private final VotanteJpaRepo repo;

    VotanteRepositorioJpa(VotanteJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public Votante guardar(Votante votante, String claveCodificada) {
        VotanteJpa fila = new VotanteJpa(
                votante.nombre(), votante.apellido(), votante.email(), claveCodificada,
                votante.codigoEstudiantil(), votante.programaAcademico(), votante.estadoVotante());
        return aDominio(repo.save(fila));
    }

    @Override
    public Optional<Votante> buscarPorId(Long id) {
        return repo.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Votante> buscarPorCodigo(String codigoEstudiantil) {
        return repo.findByCodigoEstudiantil(codigoEstudiantil).map(this::aDominio);
    }

    @Override
    public boolean existeEmail(String email) {
        return email != null && repo.existsByEmail(email.trim());
    }

    @Override
    public boolean existeCodigo(String codigoEstudiantil) {
        return codigoEstudiantil != null && repo.existsByCodigoEstudiantil(codigoEstudiantil.trim());
    }

    @Override
    public List<Votante> listar() {
        return repo.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public List<Votante> listarPorEstado(EstadoVotante estado) {
        return repo.findByEstadoVotante(estado).stream().map(this::aDominio).toList();
    }

    @Override
    public Votante actualizarEstado(Long id, EstadoVotante estado) {
        VotanteJpa fila = repo.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        fila.setEstadoVotante(estado);
        return aDominio(repo.save(fila));
    }

    private Votante aDominio(VotanteJpa fila) {
        return new Votante(
                fila.getId(), fila.getNombre(), fila.getApellido(), fila.getEmail(),
                fila.getCodigoEstudiantil(), fila.getProgramaAcademico(), fila.getEstadoVotante());
    }
}

@Entity
@Table(name = "votantes_estudiantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
class VotanteJpa extends UsuarioJpa {

    @Column(name = "codigo_estudiantil", nullable = false, unique = true, length = 50)
    private String codigoEstudiantil;

    @Column(name = "programa_academico", nullable = false, length = 150)
    private String programaAcademico;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_votante", nullable = false, length = 30)
    private EstadoVotante estadoVotante;

    protected VotanteJpa() {
    }

    VotanteJpa(String nombre, String apellido, String email, String password,
               String codigoEstudiantil, String programaAcademico, EstadoVotante estadoVotante) {
        super(nombre, apellido, email, password);
        this.codigoEstudiantil = codigoEstudiantil;
        this.programaAcademico = programaAcademico;
        this.estadoVotante = estadoVotante;
    }

    String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }

    String getProgramaAcademico() {
        return programaAcademico;
    }

    EstadoVotante getEstadoVotante() {
        return estadoVotante;
    }

    void setEstadoVotante(EstadoVotante estadoVotante) {
        this.estadoVotante = estadoVotante;
    }
}

interface VotanteJpaRepo extends JpaRepository<VotanteJpa, Long> {
    Optional<VotanteJpa> findByCodigoEstudiantil(String codigoEstudiantil);

    boolean existsByEmail(String email);

    boolean existsByCodigoEstudiantil(String codigoEstudiantil);

    List<VotanteJpa> findByEstadoVotante(EstadoVotante estadoVotante);
}
