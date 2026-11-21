package com.duoc.pixelmeet.web;

import com.duoc.pixelmeet.web.client.ApiDtos.Banner;
import com.duoc.pixelmeet.web.client.ApiDtos.Comentario;
import com.duoc.pixelmeet.web.client.ApiDtos.Compartido;
import com.duoc.pixelmeet.web.client.ApiDtos.Comunidad;
import com.duoc.pixelmeet.web.client.ApiDtos.Media;
import com.duoc.pixelmeet.web.client.ApiDtos.Valoracion;
import com.duoc.pixelmeet.web.client.ApiDtos.EventoDetalle;
import com.duoc.pixelmeet.web.client.ApiDtos.EventoResumen;
import com.duoc.pixelmeet.web.client.ApiDtos.Filtros;
import com.duoc.pixelmeet.web.client.ApiDtos.Home;
import com.duoc.pixelmeet.web.client.ApiDtos.Perfil;
import com.duoc.pixelmeet.web.security.ApiUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/** Datos de prueba que simulan las respuestas de la API. */
final class Datos {

    static final String TOKEN_GAMER = "token.gamer.firmado";
    static final String TOKEN_ADMIN = "token.admin.firmado";

    private Datos() {
    }

    static EventoResumen resumen(long id, String nombre) {
        return new EventoResumen(id, nombre, "Valorant", "Torneo", "PRESENCIAL", "Chile", "Santiago",
                LocalDateTime.of(2026, 11, 21, 10, 0), "Resumen del evento", "$20.000", 100, true,
                "valorant-1.svg");
    }

    static EventoDetalle detalle(long id, String nombre) {
        return new EventoDetalle(id, nombre, "Valorant", "Torneo", "PRESENCIAL", "Chile", "Santiago",
                LocalDateTime.of(2026, 11, 21, 10, 0), "Resumen", "Descripción completa del evento",
                "Movistar Arena", null, "Andes Esports", "Equipos de 5 jugadores", "Inscripción en línea",
                "$20.000", 32, 1840, true, List.of("valorant-1.svg", "valorant-2.svg"));
    }

    static Home home() {
        return new Home(List.of(resumen(1, "Copa Andina de Valorant")),
                List.of(resumen(2, "GamerFest Lima")),
                List.of(new Banner(1L, "NeoKeys", "Teclados con descuento", "Ver", "banner-neokeys.svg")),
                List.of(new Compartido(1L, "Copa Andina de Valorant", "diegomz", "avatar-3",
                        "<script>alert('xss')</script> ¡Vamos!", LocalDateTime.of(2026, 11, 20, 18, 0))));
    }

    static Comunidad comunidad(boolean puedeEditar) {
        return new Comunidad(1L, puedeEditar,
                List.of(new Media(7L, "FOTO", null, null, "camirojas", LocalDateTime.of(2026, 11, 20, 10, 0))),
                List.of(new Media(8L, "VIDEO", "YOUTUBE", "dQw4w9WgXcQ", "camirojas",
                        LocalDateTime.of(2026, 11, 20, 10, 5))),
                List.of(new Comentario(3L, "diegomz", "avatar-3", "<img src=x onerror=alert(1)> ¡Buenísimo!",
                        LocalDateTime.of(2026, 11, 20, 11, 0), false)),
                new Valoracion(4.5, 2, 4), 3);
    }

    static Filtros filtros() {
        return new Filtros(List.of("Valorant"), List.of("Torneo"), List.of("Chile"), List.of("Santiago"),
                List.of("PRESENCIAL", "ONLINE"));
    }

    static Perfil perfil(String cuenta) {
        return new Perfil(cuenta, "Camila Rojas", "camirojas", "avatar-2", List.of("Valorant"),
                true, false, true);
    }

    static Authentication gamer() {
        return autenticacion(new ApiUser("gamer1", "Camila Rojas", List.of("USER"), TOKEN_GAMER,
                Instant.now().plusSeconds(3600)), "USER");
    }

    static Authentication admin() {
        return autenticacion(new ApiUser("admin", "Administrador", List.of("ADMIN", "USER"), TOKEN_ADMIN,
                Instant.now().plusSeconds(3600)), "ADMIN", "USER");
    }

    private static Authentication autenticacion(ApiUser user, String... roles) {
        var auths = java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        return UsernamePasswordAuthenticationToken.authenticated(user, null, auths);
    }
}
