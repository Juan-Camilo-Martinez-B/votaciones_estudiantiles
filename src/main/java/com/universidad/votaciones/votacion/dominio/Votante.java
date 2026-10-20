package com.universidad.votaciones.votacion.dominio;

public record Votante(
        Long id,
        String nombre,
        String apellido,
        String email,
        String codigoEstudiantil,
        String programaAcademico,
        EstadoVotante estadoVotante) {

    public Votante {
        nombre = exigir(nombre, "El nombre es obligatorio.");
        apellido = exigir(apellido, "El apellido es obligatorio.");
        email = exigir(email, "El email es obligatorio.");
        codigoEstudiantil = exigir(codigoEstudiantil, "El código estudiantil es obligatorio.");
        programaAcademico = exigir(programaAcademico, "El programa académico es obligatorio.");
        if (estadoVotante == null) {
            throw new ReglaDeNegocioVioladaException("El estado del votante es obligatorio.");
        }
    }

    public static Votante pendiente(String nombre, String apellido, String email,
                                    String codigoEstudiantil, String programaAcademico) {
        return new Votante(null, nombre, apellido, email, codigoEstudiantil, programaAcademico,
                EstadoVotante.PENDIENTE_VERIFICACION);
    }

    public Votante conEstado(EstadoVotante estado) {
        return new Votante(id, nombre, apellido, email, codigoEstudiantil, programaAcademico, estado);
    }

    public boolean estaHabilitado() {
        return estadoVotante == EstadoVotante.HABILITADO;
    }

    private static String exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
        return valor.trim();
    }
}
