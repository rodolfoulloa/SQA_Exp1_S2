package com.duoc.pixelmeet.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventoPublicarApiTest extends ApiTestBase {

    private static final String EVENTO_PRESENCIAL = """
            {"nombre":"Torneo Smash Valpo","videojuego":"Super Smash Bros","tipo":"Torneo",
             "modalidad":"PRESENCIAL","pais":"Chile","ciudad":"Valparaíso",
             "fechaHora":"2026-12-12T18:30:00","resumen":"Torneo abierto de Smash en Valparaíso",
             "descripcion":"Bracket doble eliminación, consolas provistas por la organización.",
             "ubicacion":"Av. Brasil 2950, Valparaíso","enlaceAcceso":"",
             "requisitos":"Traer control propio","inscripcion":"En el lugar desde las 17:00",
             "precio":"Gratis","cupos":64,"organizador":"Suplantado"}
            """;

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(post("/api/eventos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(EVENTO_PRESENCIAL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioAutenticadoPublicaYElOrganizadorSaleDelToken() throws Exception {
        String token = login("gamer1", "Gamer123!");
        String json = mvc.perform(post("/api/eventos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(EVENTO_PRESENCIAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Torneo Smash Valpo"))
                .andExpect(jsonPath("$.organizador").value(org.hamcrest.Matchers.not("Suplantado")))
                .andExpect(jsonPath("$.enlaceAcceso").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Integer id = com.jayway.jsonpath.JsonPath.read(json, "$.id");

        mvc.perform(get("/api/eventos").param("nombre", "smash valpo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void datosInvalidosDevuelven400ConCampos() throws Exception {
        String token = login("gamer2", "Gamer123!");
        mvc.perform(post("/api/eventos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"x","videojuego":"","tipo":"Torneo","modalidad":"ONLINE",
                                 "pais":"<script>","ciudad":"Santiago","fechaHora":"2026-12-12T18:30:00",
                                 "resumen":"corto","descripcion":"Descripción suficiente","requisitos":"Ninguno",
                                 "inscripcion":"Web","precio":"Gratis","enlaceAcceso":"javascript:alert(1)"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists())
                .andExpect(jsonPath("$.campos.videojuego").exists())
                .andExpect(jsonPath("$.campos.pais").exists())
                .andExpect(jsonPath("$.campos.resumen").exists())
                .andExpect(jsonPath("$.campos.enlaceAcceso").exists());
    }

    @Test
    void fechaPasadaYUbicacionFaltanteSeRechazan() throws Exception {
        String token = login("gamer2", "Gamer123!");
        mvc.perform(post("/api/eventos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_PRESENCIAL.replace("2026-12-12", "2026-10-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.fechaHora").exists());

        mvc.perform(post("/api/eventos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EVENTO_PRESENCIAL.replace("Av. Brasil 2950, Valparaíso", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.ubicacion").exists());
    }
}
