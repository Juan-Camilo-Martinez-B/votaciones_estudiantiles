package com.universidad.votaciones.voting;

public class VotoRequestDTO {
    private String idEstudiante;
    private String idCandidato;

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