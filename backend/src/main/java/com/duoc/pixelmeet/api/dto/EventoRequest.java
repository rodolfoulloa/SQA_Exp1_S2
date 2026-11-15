package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Modalidad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Datos para publicar un evento. El organizador y la fecha de publicacion no vienen en la peticion:
 * los asigna el servidor a partir del usuario del token.
 */
public record EventoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
        String nombre,

        @NotBlank(message = "El videojuego es obligatorio")
        @Size(min = 2, max = 60, message = "El videojuego debe tener entre 2 y 60 caracteres")
        String videojuego,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(min = 2, max = 40, message = "El tipo debe tener entre 2 y 40 caracteres")
        String tipo,

        @NotNull(message = "Selecciona la modalidad")
        Modalidad modalidad,

        @NotBlank(message = "El país es obligatorio")
        @Size(min = 2, max = 60, message = "El país debe tener entre 2 y 60 caracteres")
        @Pattern(regexp = TEXTO_GEO, message = "El país solo puede tener letras y espacios")
        String pais,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 2, max = 60, message = "La ciudad debe tener entre 2 y 60 caracteres")
        @Pattern(regexp = TEXTO_GEO, message = "La ciudad solo puede tener letras y espacios")
        String ciudad,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaHora,

        @NotBlank(message = "El resumen es obligatorio")
        @Size(min = 10, max = 200, message = "El resumen debe tener entre 10 y 200 caracteres")
        String resumen,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 3000, message = "La descripción debe tener entre 10 y 3000 caracteres")
        String descripcion,

        @Size(max = 200, message = "La ubicación no puede superar 200 caracteres")
        String ubicacion,

        @Size(max = 300, message = "El enlace no puede superar 300 caracteres")
        @Pattern(regexp = "^$|^https://[^\\s<>\"']+$", message = "El enlace debe comenzar con https://")
        String enlaceAcceso,

        @NotBlank(message = "Los requisitos son obligatorios")
        @Size(max = 1000, message = "Los requisitos no pueden superar 1000 caracteres")
        String requisitos,

        @NotBlank(message = "La inscripción es obligatoria")
        @Size(max = 1000, message = "La inscripción no puede superar 1000 caracteres")
        String inscripcion,

        @NotBlank(message = "El precio es obligatorio")
        @Size(max = 40, message = "El precio no puede superar 40 caracteres")
        String precio,

        @Min(value = 1, message = "Los cupos deben ser al menos 1")
        @Max(value = 100000, message = "Los cupos no pueden superar 100.000")
        Integer cupos) {

    /** Letras (con tildes), espacios, punto, apostrofe y guion. */
    public static final String TEXTO_GEO = "^[\\p{L} .'-]+$";
}
