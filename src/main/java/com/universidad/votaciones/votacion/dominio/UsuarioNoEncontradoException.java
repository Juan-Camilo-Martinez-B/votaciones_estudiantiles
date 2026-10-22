package com.universidad.votaciones.votacion.dominio;

public class UsuarioNoEncontradoException extends ReglaDeNegocioVioladaException {
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public UsuarioNoEncontradoException(Long id) {
        super("No se encontró el usuario con id: " + id);
    }
}
