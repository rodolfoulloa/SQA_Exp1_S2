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

    public record Banner(Long id, String marca, String mensaje, String llamado, String imagen) {
    }

    public record Home(List<EventoResumen> recientes, List<EventoResumen> populares, List<Banner> banners) {
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
