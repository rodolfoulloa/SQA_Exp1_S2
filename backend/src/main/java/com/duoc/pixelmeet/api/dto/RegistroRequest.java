package com.duoc.pixelmeet.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "El nombre solo puede tener letras y espacios")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Ingresa un correo válido")
        @Size(max = 100, message = "El correo no puede superar 100 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe tener letras y números")
        String password) {
}
