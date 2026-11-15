package com.duoc.pixelmeet.api;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proteccion CSRF real de la API (double submit cookie), sin el atajo csrf() de spring-security-test:
 * ese atajo reemplaza el repositorio de tokens del contexto, por eso esta clase usa un contexto propio.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CsrfApiTest extends ApiTestBase {

    private static final String LOGIN = "{\"username\":\"gamer1\",\"password\":\"Gamer123!\"}";

    private record Csrf(Cookie cookie, String valor) {
    }

    private Csrf obtenerToken() throws Exception {
        var res = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.header").value("X-XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", true))
                .andExpect(cookie().secure("XSRF-TOKEN", true))
                .andReturn().getResponse();
        String valor = com.jayway.jsonpath.JsonPath.read(res.getContentAsString(), "$.token");
        Cookie c = res.getCookie("XSRF-TOKEN");
        assertThat(c).isNotNull();
        assertThat(c.getValue()).isEqualTo(valor);
        return new Csrf(c, valor);
    }

    @Test
    void sinTokenCsrfLasPeticionesQueModificanSeRechazan() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/perfil").header("Authorization", "Bearer x")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conElTokenEmitidoPorLaApiLaPeticionSeAcepta() throws Exception {
        Csrf csrf = obtenerToken();
        mvc.perform(post("/api/auth/login").cookie(csrf.cookie()).header("X-XSRF-TOKEN", csrf.valor())
                        .contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void headerDistintoALaCookieSeRechaza() throws Exception {
        Csrf csrf = obtenerToken();
        mvc.perform(post("/api/auth/login").cookie(csrf.cookie()).header("X-XSRF-TOKEN", "valor-forjado")
                        .contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").header("X-XSRF-TOKEN", csrf.valor())
                        .contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isForbidden());
    }
}
