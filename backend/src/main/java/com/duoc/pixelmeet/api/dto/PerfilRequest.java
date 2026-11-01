package com.duoc.pixelmeet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PerfilRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9_]{3,20}$",
                message = "Usa entre 3 y 20 caracteres: letras, números o guion bajo")
        String nombreUsuario,

        @NotBlank(message = "Selecciona un avatar")
        String avatar,

        @Size(max = 12, message = "Selecciona como máximo 12 juegos")
        List<String> juegos,

        Boolean notifCorreo,
        Boolean notifPush,
        Boolean notifNuevosEventos) {
}
