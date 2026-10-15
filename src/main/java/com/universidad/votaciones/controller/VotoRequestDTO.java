package com.universidad.votaciones.controller;

public class VotoRequestDTO {
    private String idEstudiante;
    private String idCandidato;

    // Getters y Setters
    public String getIdEstudiante() {
        return idEstudiante;
    }
    public void setIdEstudiante(String idEstudiante) {
        this.idEstudiante = idEstudiante;
    }
    public String getIdCandidato() {
        return idCandidato;
    }
    public void setIdCandidato(String idCandidato) {
        this.idCandidato = idCandidato;
    }
}