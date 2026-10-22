package com.universidad.votaciones.votacion.infraestructura.salida.seguridad;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.universidad.votaciones.votacion.aplicacion.puertos.salida.CodificadorDeClaves;

@Component
public class CodificadorArgon2 implements CodificadorDeClaves {

    private final PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @Override
    public String codificar(String clavePlana) {
        return encoder.encode(clavePlana);
    }
}
