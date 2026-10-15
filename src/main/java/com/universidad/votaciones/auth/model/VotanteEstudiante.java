package com.universidad.votaciones.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "votantes_estudiantes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class VotanteEstudiante extends Usuario {

    @Column(name = "codigo_estudiantil", nullable = false, unique = true, length = 50)
    private String codigoEstudiantil;

    @Column(name = "programa_academico", nullable = false, length = 150)
    private String programaAcademico;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_votante", nullable = false, length = 30)
    private EstadoVotante estadoVotante;

    public VotanteEstudiante() {
    }

    public VotanteEstudiante(String nombre, String apellido, String email, String password,
                             String codigoEstudiantil, String programaAcademico, EstadoVotante estadoVotante) {
        super(nombre, apellido, email, password);
        this.codigoEstudiantil = codigoEstudiantil;
        this.programaAcademico = programaAcademico;
        this.estadoVotante = estadoVotante;
    }

    public String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }

    public void setCodigoEstudiantil(String codigoEstudiantil) {
        this.codigoEstudiantil = codigoEstudiantil;
    }

    public String getProgramaAcademico() {
        return programaAcademico;
    }

    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }

    public EstadoVotante getEstadoVotante() {
        return estadoVotante;
    }

    public void setEstadoVotante(EstadoVotante estadoVotante) {
        this.estadoVotante = estadoVotante;
    }
}
