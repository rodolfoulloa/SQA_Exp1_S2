package com.duoc.pixelmeet.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeguridadRutasApiTest extends ApiTestBase {

    @Value("${app.jwt.secret}")
    private String secreto;

    // ---------- Rutas publicas ----------

    @Test
    void homeEsPublico() throws Exception {
        mvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recientes").isNotEmpty())
                .andExpect(jsonPath("$.populares").isNotEmpty())
                .andExpect(jsonPath("$.banners.length()").value(3));
    }

    @Test
    void busquedaEsPublicaYSoloEntregaResumen() throws Exception {
        mvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[0].descripcion").doesNotExist())
                .andExpect(jsonPath("$[0].requisitos").doesNotExist())
                .andExpect(jsonPath("$[0].enlaceAcceso").doesNotExist());
    }

    @Test
    void filtrosSonPublicos() throws Exception {
        mvc.perform(get("/api/eventos/filtros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modalidades.length()").value(2));
    }

    // ---------- Rutas privadas ----------

    @Test
    void detalleSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/eventos/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void detalleConTokenValidoDevuelveTodaLaInformacion() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").exists())
                .andExpect(jsonPath("$.organizador").exists())
                .andExpect(jsonPath("$.requisitos").exists())
                .andExpect(jsonPath("$.inscripcion").exists())
                .andExpect(jsonPath("$.fotos").isNotEmpty());
    }

    @Test
    void detalleInexistenteDevuelve404() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(get("/api/eventos/99999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void tokenAlteradoEsRechazado() throws Exception {
        String token = login("gamer1", "Gamer123!");
        String[] partes = token.split("\\.");
        // Se cambia la firma: el payload sigue igual pero ya no coincide.
        String alterado = partes[0] + "." + partes[1] + "." + new StringBuilder(partes[2]).reverse();
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(alterado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFirmadoConOtraClaveEsRechazado() throws Exception {
        SecretKey otra = Jwts.SIG.HS256.key().build();
        String falso = Jwts.builder().issuer("pixelmeet-api").subject("admin")
                .claim("roles", List.of("ROLE_ADMIN"))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(otra).compact();
        mvc.perform(get("/api/admin/usuarios").header("Authorization", bearer(falso)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinFirmaEsRechazado() throws Exception {
        String sinFirma = Jwts.builder().issuer("pixelmeet-api").subject("admin")
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))).compact();
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(sinFirma)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpiradoEsRechazado() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        String expirado = Jwts.builder().issuer("pixelmeet-api").subject("gamer1")
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(key).compact();
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(expirado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeOtroEmisorEsRechazado() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        String otroEmisor = Jwts.builder().issuer("otra-app").subject("gamer1")
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(key).compact();
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(otroEmisor)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void perfilRequiereToken() throws Exception {
        mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    // ---------- Autorizacion por roles ----------

    @Test
    void usuarioNormalNoAccedeAAdmin() throws Exception {
        String token = login("gamer2", "Gamer123!");
        mvc.perform(get("/api/admin/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.estado").value(403));
    }

    @Test
    void adminAccedeAAdminYNoSeExponenClaves() throws Exception {
        String token = login("admin", "Admin123!");
        mvc.perform(get("/api/admin/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void rutasNoDeclaradasQuedanCerradas() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        String token = login("admin", "Admin123!");
        mvc.perform(post("/api/eventos").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cabecerasDeSeguridadPresentes() throws Exception {
        mvc.perform(get("/api/home"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void apiNoCreaSesionHttp() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer(token)))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}
