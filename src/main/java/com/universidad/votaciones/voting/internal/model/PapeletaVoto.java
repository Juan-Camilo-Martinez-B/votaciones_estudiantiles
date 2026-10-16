package com.universidad.votaciones.voting.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Representa la urna digital anónima (ADR-002).
 * Por estricta regla de voto secreto y anonimato institucional,
 * esta entidad NO posee ninguna llave foránea ni vínculo relacional
 * hacia la tabla de identidad de los estudiantes.
 */
@Entity
@Table(name = "papeletas_voto")
public class PapeletaVoto {

    @Id
    private String idPapeleta;

    @Column(nullable = false)
    private String idCandidato;

    @Column(nullable = false, unique = true)
    private String tokenAnonimo;

    @Column(nullable = false)
    private LocalDateTime fechaHoraRegistro;

    public PapeletaVoto() {
    }

    public PapeletaVoto(String idPapeleta, String idCandidato, String tokenAnonimo, LocalDateTime fechaHoraRegistro) {
        this.idPapeleta = idPapeleta;
        this.idCandidato = idCandidato;
        this.tokenAnonimo = tokenAnonimo;
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public String getIdPapeleta() {
        return idPapeleta;
    }

    public void setIdPapeleta(String idPapeleta) {
        this.idPapeleta = idPapeleta;
    }

    public String getIdCandidato() {
        return idCandidato;
    }

    public void setIdCandidato(String idCandidato) {
        this.idCandidato = idCandidato;
    }

    public String getTokenAnonimo() {
        return tokenAnonimo;
    }

    public void setTokenAnonimo(String tokenAnonimo) {
        this.tokenAnonimo = tokenAnonimo;
    }

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }
}
