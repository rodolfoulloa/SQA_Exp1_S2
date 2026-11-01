package com.duoc.pixelmeet.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends ApiTestBase {

    @Test
    void loginCorrectoDevuelveJwtYRoles() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$")))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "USER")));
    }

    @Test
    void losTresUsuariosInicialesPuedenIniciarSesion() throws Exception {
        login("admin", "Admin123!");
        login("gamer1", "Gamer123!");
        login("gamer2", "Gamer123!");
    }

    @Test
    void sePuedeIniciarSesionConElCorreo() throws Exception {
        login("gamer1@pixelmeet.cl", "Gamer123!");
    }

    @Test
    void claveIncorrectaDevuelve401SinDetalles() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"gamer1\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void usuarioInexistenteRecibeElMismoMensaje() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"noexiste\",\"password\":\"Algo1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void loginSinDatosDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.username").exists());
    }

    @Test
    void jsonMalformadoDevuelve400SinTraza() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{nope"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    void bloqueaTrasCincoIntentosFallidos() throws Exception {
        String body = "{\"username\":\"fuerzabruta\",\"password\":\"Mala12345\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void registroCreaUsuarioConRolUserYPermiteLogin() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana Pérez\",\"correo\":\"Ana.Perez@Correo.cl\",\"password\":\"Clave1234\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ana.perez@correo.cl"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("USER")))
                .andExpect(jsonPath("$.password").doesNotExist());

        String token = login("ana.perez@correo.cl", "Clave1234");
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"));
    }

    @Test
    void registroDuplicadoDevuelve409() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Otro\",\"correo\":\"gamer1@pixelmeet.cl\",\"password\":\"Clave1234\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.correo").exists());
    }

    @Test
    void registroConClaveDebilDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Débil\",\"correo\":\"debil@correo.cl\",\"password\":\"12345678\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void registroNoPermiteEscalarARolAdmin() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pirata\",\"correo\":\"pirata@correo.cl\",\"password\":\"Clave1234\","
                                + "\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", not(hasItem("ADMIN"))));
    }
}
