package com.universidad.votaciones.voting.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidatos")
public class Candidato {

    @Id
    private String idCandidato;

    @Column(nullable = false, unique = true)
    private String numeroTarjeton;

    @Column(nullable = false)
    private String nombreCandidato;

    @Column(length = 1000)
    private String propuestaResumen;

    public Candidato() {
    }

    public Candidato(String idCandidato, String numeroTarjeton, String nombreCandidato, String propuestaResumen) {
        this.idCandidato = idCandidato;
        this.numeroTarjeton = numeroTarjeton;
        this.nombreCandidato = nombreCandidato;
        this.propuestaResumen = propuestaResumen;
    }

    public String getIdCandidato() {
        return idCandidato;
    }

    public void setIdCandidato(String idCandidato) {
        this.idCandidato = idCandidato;
    }

    public String getNumeroTarjeton() {
        return numeroTarjeton;
    }

    public void setNumeroTarjeton(String numeroTarjeton) {
        this.numeroTarjeton = numeroTarjeton;
    }

    public String getNombreCandidato() {
        return nombreCandidato;
    }

    public void setNombreCandidato(String nombreCandidato) {
        this.nombreCandidato = nombreCandidato;
    }

    public String getPropuestaResumen() {
        return propuestaResumen;
    }

    public void setPropuestaResumen(String propuestaResumen) {
        this.propuestaResumen = propuestaResumen;
    }
}
