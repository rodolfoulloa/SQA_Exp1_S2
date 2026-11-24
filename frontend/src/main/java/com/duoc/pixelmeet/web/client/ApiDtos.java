package com.duoc.pixelmeet.web.client;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Estructuras JSON intercambiadas con la API (espejo de los DTO del backend). */
public final class ApiDtos {

    private ApiDtos() {
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String tipo, Instant expiraEn, String username, String nombre,
                                List<String> roles) {
    }

    public record RegistroRequest(String nombre, String correo, String password) {
    }

    public record Usuario(Long id, String username, String email, String nombre, boolean activo,
                          List<String> roles) implements Serializable {
    }

    public record EventoResumen(Long id, String nombre, String videojuego, String tipo, String modalidad,
                                String pais, String ciudad, LocalDateTime fechaHora, String resumen,
                                String precio, int interesados, boolean destacado, String portada) {

        public boolean isOnline() {
            return "ONLINE".equals(modalidad);
        }
    }

    public record EventoDetalle(Long id, String nombre, String videojuego, String tipo, String modalidad,
                                String pais, String ciudad, LocalDateTime fechaHora, String resumen,
                                String descripcion, String ubicacion, String enlaceAcceso, String organizador,
                                String requisitos, String inscripcion, String precio, Integer cupos,
                                int interesados, boolean destacado, List<String> fotos) {

        public boolean isOnline() {
            return "ONLINE".equals(modalidad);
        }
    }

    public record EventoRequest(String nombre, String videojuego, String tipo, String modalidad, String pais,
                                String ciudad, LocalDateTime fechaHora, String resumen, String descripcion,
                                String ubicacion, String enlaceAcceso, String requisitos, String inscripcion,
                                String precio, Integer cupos) {
    }

    public record Banner(Long id, String marca, String mensaje, String llamado, String imagen) {
    }

    public record Home(List<EventoResumen> recientes, List<EventoResumen> populares, List<Banner> banners,
                       List<Compartido> compartidos) {

        public Home {
            compartidos = compartidos == null ? List.of() : compartidos;
        }
    }

    // ---------------- Comunidad: fotos y videos, comentarios, valoraciones y compartir ----------------

    public record Media(Long id, String tipo, String plataforma, String videoId, String subidoPor,
                        LocalDateTime creadoEn) {

        /** URL del video en su plataforma, armada solo con el id validado por la API. */
        public String urlVideo() {
            if ("YOUTUBE".equals(plataforma)) {
                return "https://www.youtube.com/watch?v=" + videoId;
            }
            if ("VIMEO".equals(plataforma)) {
                return "https://vimeo.com/" + videoId;
            }
            return null;
        }
    }

    public record Comentario(Long id, String autor, String avatar, String texto, LocalDateTime creadoEn,
                             boolean puedeEliminar) {
    }

    public record Valoracion(double promedio, long total, Integer miValoracion) {

        /** Estrellas a mostrar llenas (promedio redondeado, entre 0 y 5). */
        public int llenas() {
            return (int) Math.max(0, Math.min(5, Math.round(promedio)));
        }

        public int vacias() {
            return 5 - llenas();
        }
    }

    public record Comunidad(Long eventoId, boolean puedeEditar, List<Media> fotos, List<Media> videos,
                            List<Comentario> comentarios, Valoracion valoracion, long vecesCompartido) {
    }

    public record Compartido(Long eventoId, String evento, String usuario, String avatar, String mensaje,
                             LocalDateTime creadoEn) {
    }

    public record CompartirRequest(String canal, String mensaje) {
    }

    public record CompartirResultado(long vecesCompartido) {
    }

    public record VideoRequest(String url) {
    }

    public record ComentarioRequest(String texto) {
    }

    public record ValoracionRequest(Integer estrellas) {
    }

    /** Bytes de una foto y su tipo, tal como los entrega la API. */
    public record Foto(byte[] datos, String contentType) {

        // equals/hashCode/toString consideran el contenido del arreglo, no su referencia (java:S6218).
        @Override
        public boolean equals(Object o) {
            return o instanceof Foto otra && java.util.Arrays.equals(datos, otra.datos)
                    && java.util.Objects.equals(contentType, otra.contentType);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Arrays.hashCode(datos) + java.util.Objects.hashCode(contentType);
        }

        @Override
        public String toString() {
            return "Foto[" + (datos == null ? 0 : datos.length) + " bytes, " + contentType + "]";
        }
    }

    public record Filtros(List<String> videojuegos, List<String> tipos, List<String> paises,
                          List<String> ciudades, List<String> modalidades) {

        public static Filtros vacios() {
            return new Filtros(List.of(), List.of(), List.of(), List.of(), List.of("PRESENCIAL", "ONLINE"));
        }
    }

    public record Perfil(String cuenta, String nombre, String nombreUsuario, String avatar, List<String> juegos,
                         boolean notifCorreo, boolean notifPush, boolean notifNuevosEventos) {
    }

    public record PerfilRequest(String nombreUsuario, String avatar, List<String> juegos, boolean notifCorreo,
                                boolean notifPush, boolean notifNuevosEventos) {
    }

    public record ApiError(Instant fecha, int estado, String error, String mensaje, String ruta,
                           Map<String, String> campos) {
    }
}
