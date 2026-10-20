package com.universidad.votaciones.votacion.infraestructura.salida.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.AdministradorRepositorio;
import com.universidad.votaciones.votacion.dominio.Administrador;
import com.universidad.votaciones.votacion.dominio.UsuarioNoEncontradoException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Component
@Profile("postgres")
public class AdministradorRepositorioJpa implements AdministradorRepositorio {

    private final AdministradorJpaRepo repo;

    AdministradorRepositorioJpa(AdministradorJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public Administrador guardar(Administrador administrador, String claveCodificada) {
        AdministradorJpa fila = new AdministradorJpa(
                administrador.nombre(), administrador.apellido(), administrador.email(), claveCodificada,
                administrador.cargo(), administrador.activo());
        return aDominio(repo.save(fila));
    }

    @Override
    public Optional<Administrador> buscarPorId(Long id) {
        return repo.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existeEmail(String email) {
        return email != null && repo.existsByEmail(email.trim());
    }

    @Override
    public List<Administrador> listar() {
        return repo.findAll().stream().map(this::aDominio).toList();
    }

    @Override
    public List<Administrador> listarActivos() {
        return repo.findByActivoTrue().stream().map(this::aDominio).toList();
    }

    @Override
    public Administrador actualizarActivo(Long id, boolean activo) {
        AdministradorJpa fila = repo.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        fila.setActivo(activo);
        return aDominio(repo.save(fila));
    }

    private Administrador aDominio(AdministradorJpa fila) {
        return new Administrador(
                fila.getId(), fila.getNombre(), fila.getApellido(), fila.getEmail(),
                fila.getCargo(), Boolean.TRUE.equals(fila.getActivo()));
    }
}

@Entity
@Table(name = "usuarios_administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
class AdministradorJpa extends UsuarioJpa {

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false)
    private Boolean activo;

    protected AdministradorJpa() {
    }

    AdministradorJpa(String nombre, String apellido, String email, String password, String cargo, boolean activo) {
        super(nombre, apellido, email, password);
        this.cargo = cargo;
        this.activo = activo;
    }

    String getCargo() {
        return cargo;
    }

    Boolean getActivo() {
        return activo;
    }

    void setActivo(Boolean activo) {
        this.activo = activo;
    }
}

interface AdministradorJpaRepo extends JpaRepository<AdministradorJpa, Long> {
    boolean existsByEmail(String email);

    List<AdministradorJpa> findByActivoTrue();
}
