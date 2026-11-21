package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.CanalCompartir;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** Peticiones y respuestas de las funcionalidades de comunidad: media, comentarios, valoraciones y compartir. */
public final class ComunidadDtos {

    private ComunidadDtos() {
    }

    // ---------------- Peticiones ----------------

    public record VideoRequest(
            @NotBlank(message = "El enlace es obligatorio")
            @Size(max = 300, message = "El enlace no puede superar 300 caracteres")
            String url) {
    }

    public record ComentarioRequest(
            @NotBlank(message = "El comentario no puede estar vacío")
            @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
            String texto) {
    }

    public record ValoracionRequest(
            @NotNull(message = "Selecciona una valoración")
            @Min(value = 1, message = "La valoración mínima es 1 estrella")
            @Max(value = 5, message = "La valoración máxima es 5 estrellas")
            Integer estrellas) {
    }

    public record CompartirRequest(
            @NotNull(message = "Indica dónde compartir")
            CanalCompartir canal,
            @Size(max = 280, message = "El mensaje no puede superar 280 caracteres")
            String mensaje) {
    }

    // ---------------- Respuestas ----------------

    /** Foto o video. De los videos solo se expone plataforma e id; la URL de embebido la arma el frontend. */
    public record MediaDto(Long id, String tipo, String plataforma, String videoId, String subidoPor,
                           LocalDateTime creadoEn) {
    }

    public record ComentarioDto(Long id, String autor, String avatar, String texto, LocalDateTime creadoEn,
                                boolean puedeEliminar) {
    }

    public record ValoracionDto(double promedio, long total, Integer miValoracion) {
    }

    public record CompartidoDto(Long eventoId, String evento, String usuario, String avatar, String mensaje,
                                LocalDateTime creadoEn) {
    }

    public record CompartirResultadoDto(long vecesCompartido) {
    }

    /** Todo lo de comunidad de un evento, visto por el usuario autenticado. */
    public record EventoComunidadDto(Long eventoId, boolean puedeEditar, List<MediaDto> fotos,
                                     List<MediaDto> videos, List<ComentarioDto> comentarios,
                                     ValoracionDto valoracion, long vecesCompartido) {
    }
}
