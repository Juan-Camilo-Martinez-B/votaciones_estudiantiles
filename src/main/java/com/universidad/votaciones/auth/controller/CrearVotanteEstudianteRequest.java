package com.universidad.votaciones.auth.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearVotanteEstudianteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Size(max = 150, message = "El email no puede exceder 150 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "El código estudiantil es obligatorio")
        @Size(max = 50, message = "El código estudiantil no puede exceder 50 caracteres")
        String codigoEstudiantil,

        @NotBlank(message = "El programa académico es obligatorio")
        @Size(max = 150, message = "El programa académico no puede exceder 150 caracteres")
        String programaAcademico
) {
}
