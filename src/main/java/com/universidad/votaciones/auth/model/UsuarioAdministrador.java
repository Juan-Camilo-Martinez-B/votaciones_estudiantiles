package com.universidad.votaciones.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios_administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class UsuarioAdministrador extends Usuario {

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false)
    private Boolean activo;

    public UsuarioAdministrador() {
    }

    public UsuarioAdministrador(String nombre, String apellido, String email, String password,
                                String cargo, Boolean activo) {
        super(nombre, apellido, email, password);
        this.cargo = cargo;
        this.activo = activo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
