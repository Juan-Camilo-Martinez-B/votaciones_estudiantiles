package com.universidad.votaciones.votacion.dominio;

public record Administrador(
        Long id,
        String nombre,
        String apellido,
        String email,
        String cargo,
        boolean activo) {

    public Administrador {
        nombre = exigir(nombre, "El nombre es obligatorio.");
        apellido = exigir(apellido, "El apellido es obligatorio.");
        email = exigir(email, "El email es obligatorio.");
        cargo = exigir(cargo, "El cargo es obligatorio.");
    }

    public static Administrador nuevo(String nombre, String apellido, String email, String cargo) {
        return new Administrador(null, nombre, apellido, email, cargo, true);
    }

    public Administrador conActivo(boolean activo) {
        return new Administrador(id, nombre, apellido, email, cargo, activo);
    }

    private static String exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDeNegocioVioladaException(mensaje);
        }
        return valor.trim();
    }
}
