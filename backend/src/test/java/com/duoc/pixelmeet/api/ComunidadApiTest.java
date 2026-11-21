package com.duoc.pixelmeet.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fotos y videos, comentarios, valoraciones y compartir: acceso, permisos por recurso y validaciones. */
class ComunidadApiTest extends ApiTestBase {

    private static final String EVENTO_NUEVO = """
            {"nombre":"Meetup de prueba comunidad","videojuego":"Tekken 8","tipo":"Meetup",
             "modalidad":"PRESENCIAL","pais":"Chile","ciudad":"Santiago",
             "fechaHora":"2026-12-20T18:00:00","resumen":"Juntada para probar la comunidad",
             "descripcion":"Evento creado por gamer1 para las pruebas de comunidad.",
             "ubicacion":"Av. Siempre Viva 742","requisitos":"Ninguno","inscripcion":"En el lugar",
             "precio":"Gratis"}
            """;

    private static byte[] png(int ancho, int alto) throws Exception {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    private long publicarComo(String token) throws Exception {
        String json = mvc.perform(post("/api/eventos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(EVENTO_NUEVO))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    // ---------------- Acceso ----------------

    @Test
    void sinTokenTodoEsPrivado() throws Exception {
        mvc.perform(get("/api/eventos/1/comunidad")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/eventos/1/comentarios").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"hola\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/eventos/1/valoracion").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"estrellas\":5}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------- Fotos y videos ----------------

    @Test
    void elDuenoSubeUnaFotoQueSeRecodificaYSeSirveConSuTipo() throws Exception {
        String token = login("gamer1", "Gamer123!");
        long id = publicarComo(token);
        byte[] original = png(40, 30);
        String json = mvc.perform(multipart("/api/eventos/{id}/fotos", id)
                        .file(new MockMultipartFile("archivo", "../../evil.png", "image/png", original))
                        .with(csrf()).header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("FOTO"))
                .andReturn().getResponse().getContentAsString();
        int mediaId = JsonPath.read(json, "$.id");

        byte[] servida = mvc.perform(get("/api/eventos/{id}/media/{m}/contenido", id, mediaId)
                        .header("Authorization", bearer(login("gamer2", "Gamer123!"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("private")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(ImageIO.read(new java.io.ByteArrayInputStream(servida)).getWidth()).isEqualTo(40);

        mvc.perform(get("/api/eventos/{id}/comunidad", id).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.puedeEditar").value(true))
                .andExpect(jsonPath("$.fotos", hasSize(1)));
    }

    @Test
    void archivoQueNoEsImagenSeRechazaAunqueDigaSerPng() throws Exception {
        String token = login("gamer1", "Gamer123!");
        long id = publicarComo(token);
        mvc.perform(multipart("/api/eventos/{id}/fotos", id)
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png",
                                "<script>alert(1)</script>".getBytes()))
                        .with(csrf()).header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.archivo").value("Solo se permiten imágenes JPG o PNG"));
    }

    @Test
    void imagenGiganteSeRechazaAntesDeDecodificar() throws Exception {
        String token = login("gamer1", "Gamer123!");
        long id = publicarComo(token);
        mvc.perform(multipart("/api/eventos/{id}/fotos", id)
                        .file(new MockMultipartFile("archivo", "grande.png", "image/png", png(4001, 2)))
                        .with(csrf()).header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.archivo").exists());
    }

    @Test
    void otroUsuarioNoPuedeAgregarMediaAUnEventoAjeno() throws Exception {
        long id = publicarComo(login("gamer1", "Gamer123!"));
        String ajeno = login("gamer2", "Gamer123!");
        mvc.perform(multipart("/api/eventos/{id}/fotos", id)
                        .file(new MockMultipartFile("archivo", "f.png", "image/png", png(10, 10)))
                        .with(csrf()).header("Authorization", bearer(ajeno)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/eventos/{id}/videos", id).with(csrf()).header("Authorization", bearer(ajeno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://youtu.be/dQw4w9WgXcQ\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/eventos/{id}/comunidad", id).header("Authorization", bearer(ajeno)))
                .andExpect(jsonPath("$.puedeEditar").value(false));
    }

    @Test
    void videosSoloDeYoutubeOVimeoYSeGuardaSoloElId() throws Exception {
        String token = login("admin", "Admin123!");
        mvc.perform(post("/api/eventos/1/videos").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plataforma").value("YOUTUBE"))
                .andExpect(jsonPath("$.videoId").value("dQw4w9WgXcQ"));
        for (String malo : new String[]{"javascript:alert(1)", "http://youtu.be/dQw4w9WgXcQ",
                "https://evil.com/youtube.com/watch?v=dQw4w9WgXcQ", "https://youtu.be/abc\"onerror=x"}) {
            mvc.perform(post("/api/eventos/1/videos").with(csrf()).header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":" + com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                                    .writeValueAsString(malo) + "}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.url").exists());
        }
    }

    @Test
    void elDuenoEliminaMediaYNoSePuedeLeerMediaDesdeOtroEvento() throws Exception {
        String token = login("gamer1", "Gamer123!");
        long id = publicarComo(token);
        String json = mvc.perform(multipart("/api/eventos/{id}/fotos", id)
                        .file(new MockMultipartFile("archivo", "f.png", "image/png", png(8, 8)))
                        .with(csrf()).header("Authorization", bearer(token)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int mediaId = JsonPath.read(json, "$.id");
        // Mismo id de media, pero bajo otro evento: no se entrega.
        mvc.perform(get("/api/eventos/1/media/{m}/contenido", mediaId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/eventos/{id}/media/{m}", id, mediaId).with(csrf())
                        .header("Authorization", bearer(login("gamer2", "Gamer123!"))))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/eventos/{id}/media/{m}", id, mediaId).with(csrf())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/eventos/{id}/media/{m}/contenido", id, mediaId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    // ---------------- Comentarios ----------------

    @Test
    void comentarioSeGuardaTalCualYSoloElAutorOAdminLoEliminan() throws Exception {
        String autor = login("gamer2", "Gamer123!");
        String json = mvc.perform(post("/api/eventos/2/comentarios").with(csrf()).header("Authorization", bearer(autor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"  ¡Nos vemos ahí! <b>gg</b>  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("¡Nos vemos ahí! <b>gg</b>"))
                .andExpect(jsonPath("$.autor").value("diegomz"))
                .andReturn().getResponse().getContentAsString();
        int cid = JsonPath.read(json, "$.id");

        String otro = login("gamer1", "Gamer123!");
        mvc.perform(get("/api/eventos/2/comunidad").header("Authorization", bearer(otro)))
                .andExpect(jsonPath("$.comentarios[?(@.id == " + cid + ")].puedeEliminar").value(false));
        mvc.perform(delete("/api/eventos/2/comentarios/{c}", cid).with(csrf()).header("Authorization", bearer(otro)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/eventos/2/comentarios/{c}", cid).with(csrf())
                        .header("Authorization", bearer(login("admin", "Admin123!"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void comentarioVacioOLargoSeRechaza() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(post("/api/eventos/2/comentarios").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/eventos/2/comentarios").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"" + "a".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.texto").exists());
        mvc.perform(post("/api/eventos/9999/comentarios").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"hola\"}"))
                .andExpect(status().isNotFound());
    }

    // ---------------- Valoraciones ----------------

    @Test
    void cadaUsuarioValoraUnaSolaVezYSeCalculaElPromedio() throws Exception {
        String g1 = login("gamer1", "Gamer123!");
        String g2 = login("gamer2", "Gamer123!");
        mvc.perform(put("/api/eventos/3/valoracion").with(csrf()).header("Authorization", bearer(g1))
                .contentType(MediaType.APPLICATION_JSON).content("{\"estrellas\":2}")).andExpect(status().isOk());
        // Volver a valorar actualiza el voto, no suma otro.
        mvc.perform(put("/api/eventos/3/valoracion").with(csrf()).header("Authorization", bearer(g1))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estrellas\":4}"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.miValoracion").value(4));
        mvc.perform(put("/api/eventos/3/valoracion").with(csrf()).header("Authorization", bearer(g2))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estrellas\":5}"))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.promedio").value(4.5));
        mvc.perform(put("/api/eventos/3/valoracion").with(csrf()).header("Authorization", bearer(g2))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estrellas\":6}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Compartir ----------------

    @Test
    void compartirEnElSitioApareceEnElHomePublico() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(post("/api/eventos/4/compartir").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canal\":\"SITIO\",\"mensaje\":\"¡Vamos todos a este evento!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vecesCompartido").isNumber());
        mvc.perform(post("/api/eventos/4/compartir").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"canal\":\"WHATSAPP\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartidos[0].mensaje").value("¡Vamos todos a este evento!"))
                .andExpect(jsonPath("$.compartidos[0].usuario").value("camirojas"))
                .andExpect(jsonPath("$.compartidos[*].mensaje", not(org.hamcrest.Matchers.hasItem(
                        org.hamcrest.Matchers.nullValue()))));
        mvc.perform(post("/api/eventos/4/compartir").with(csrf()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"canal\":\"TELEGRAM\"}"))
                .andExpect(status().isBadRequest());
    }
}
