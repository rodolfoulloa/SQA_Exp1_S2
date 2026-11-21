package com.duoc.pixelmeet.web;

import com.duoc.pixelmeet.web.client.ApiDtos.CompartirResultado;
import com.duoc.pixelmeet.web.client.ApiDtos.Foto;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Vistas y acciones de comunidad: fotos y videos, comentarios, valoraciones y compartir. */
@SpringBootTest
@AutoConfigureMockMvc
class ComunidadWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PixelMeetApiClient api;

    @BeforeEach
    void apiSimulada() {
        when(api.detalle(anyLong(), anyString())).thenReturn(Datos.detalle(1, "Copa Andina de Valorant"));
        when(api.comunidad(anyLong(), anyString())).thenReturn(Datos.comunidad(false));
        when(api.home()).thenReturn(Datos.home());
    }

    // ---------------- Vistas ----------------

    @Test
    void detalleMuestraGaleriaComentariosValoracionYCompartirEscapandoElContenido() throws Exception {
        mvc.perform(get("/eventos/1").with(authentication(Datos.gamer())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/eventos/1/media/7")))
                .andExpect(content().string(containsString("https://www.youtube.com/watch?v=dQw4w9WgXcQ")))
                .andExpect(content().string(containsString("4,5 / 5")))
                .andExpect(content().string(containsString("Compartido 3 veces")))
                .andExpect(content().string(containsString("&lt;img src=x onerror=alert(1)&gt;")))
                .andExpect(content().string(not(containsString("<img src=x onerror"))))
                // Sin permiso de edicion no aparece el formulario de subida.
                .andExpect(content().string(not(containsString("Subir foto"))));
    }

    @Test
    void elDuenoVeElFormularioDeSubidaConCsrf() throws Exception {
        when(api.comunidad(anyLong(), anyString())).thenReturn(Datos.comunidad(true));
        mvc.perform(get("/eventos/1").with(authentication(Datos.gamer())))
                .andExpect(content().string(allOf(containsString("Subir foto"),
                        containsString("enctype=\"multipart/form-data\""), containsString("name=\"_csrf\""))));
    }

    @Test
    void homeMuestraLoCompartidoEscapado() throws Exception {
        mvc.perform(get("/home"))
                .andExpect(content().string(containsString("Compartidos por la comunidad")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert('xss')"))));
    }

    // ---------------- Acciones ----------------

    @Test
    void accionesExigenLoginYCsrf() throws Exception {
        mvc.perform(post("/eventos/1/comentarios").with(csrf()).param("texto", "hola"))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(post("/eventos/1/comentarios").with(authentication(Datos.gamer())).param("texto", "hola"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/eventos/1/media/7")).andExpect(redirectedUrl("/login"));
        verify(api, never()).comentar(anyLong(), anyString(), anyString());
    }

    @Test
    void subirFotoReenviaElArchivoConElJwt() throws Exception {
        byte[] datos = {(byte) 0x89, 'P', 'N', 'G'};
        mvc.perform(multipart("/eventos/1/fotos").file(new MockMultipartFile("archivo", "f.png", "image/png", datos))
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#galeria"))
                .andExpect(flash().attribute("mensaje", "Foto agregada al evento"));
        verify(api).subirFoto(eq(1L), eq(datos), eq("f.png"), eq(Datos.TOKEN_GAMER));
    }

    @Test
    void errorDeValidacionDeLaApiSeMuestraEnLaPagina() throws Exception {
        when(api.subirFoto(anyLong(), any(), any(), anyString())).thenThrow(new ApiException(
                ApiException.Tipo.VALIDACION, "Datos inválidos", Map.of("archivo", "Solo se permiten imágenes JPG o PNG")));
        mvc.perform(multipart("/eventos/1/fotos").file(new MockMultipartFile("archivo", "x.png", "image/png", new byte[]{1}))
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(flash().attribute("error", "Solo se permiten imágenes JPG o PNG"));
    }

    @Test
    void sinPermisoSeAvisaSinRomperLaPagina() throws Exception {
        when(api.agregarVideo(anyLong(), anyString(), anyString())).thenThrow(new ApiException(
                ApiException.Tipo.PROHIBIDO, "Prohibido", Map.of()));
        mvc.perform(post("/eventos/1/videos").param("url", "https://youtu.be/dQw4w9WgXcQ")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(flash().attribute("error", "No tienes permisos para esta acción"));
    }

    @Test
    void fotoSeEntregaSoloSiEsUnaImagenConocida() throws Exception {
        when(api.foto(1L, 7L, Datos.TOKEN_GAMER)).thenReturn(new Foto(new byte[]{1, 2}, "image/png"));
        mvc.perform(get("/eventos/1/media/7").with(authentication(Datos.gamer())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        when(api.foto(1L, 8L, Datos.TOKEN_GAMER)).thenReturn(new Foto(new byte[]{1}, "text/html"));
        mvc.perform(get("/eventos/1/media/8").with(authentication(Datos.gamer())))
                .andExpect(status().isNotFound());
    }

    @Test
    void comentarValorarYEliminar() throws Exception {
        mvc.perform(post("/eventos/1/comentarios").param("texto", "¡Nos vemos!")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#comentarios"));
        verify(api).comentar(1L, "¡Nos vemos!", Datos.TOKEN_GAMER);

        mvc.perform(post("/eventos/1/valoracion").param("estrellas", "5")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#valoracion"));
        verify(api).valorar(1L, 5, Datos.TOKEN_GAMER);

        mvc.perform(post("/eventos/1/comentarios/3/eliminar").with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#comentarios"));
        verify(api).eliminarComentario(1L, 3L, Datos.TOKEN_GAMER);
    }

    @Test
    void compartirEnElSitioYEnRedesSinRedireccionAbierta() throws Exception {
        when(api.compartir(anyLong(), anyString(), any(), anyString())).thenReturn(new CompartirResultado(4));
        mvc.perform(post("/eventos/1/compartir").param("canal", "SITIO").param("mensaje", "¡Vamos!")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#compartir"));
        verify(api).compartir(1L, "SITIO", "¡Vamos!", Datos.TOKEN_GAMER);

        mvc.perform(post("/eventos/1/compartir").param("canal", "WHATSAPP").param("mensaje", "ignorado")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("https://wa.me/?text=Mira%20este%20evento%20en%20PixelMeet:%20Copa%20Andina%20de%20Valorant%20http://localhost/eventos/1"));
        verify(api).compartir(eq(1L), eq("WHATSAPP"), isNull(), eq(Datos.TOKEN_GAMER));

        mvc.perform(post("/eventos/1/compartir").param("canal", "https://evil.example")
                        .with(authentication(Datos.gamer())).with(csrf()))
                .andExpect(redirectedUrl("/eventos/1#compartir"))
                .andExpect(flash().attribute("error", "Canal para compartir no permitido"));
    }

    @Test
    void enlacesDeRedesUsanSoloDominiosPermitidos() throws Exception {
        when(api.compartir(anyLong(), anyString(), any(), anyString())).thenReturn(new CompartirResultado(1));
        for (String[] caso : new String[][]{{"X", "https://twitter.com/intent/tweet?"},
                {"FACEBOOK", "https://www.facebook.com/sharer/sharer.php?u="},
                {"LINKEDIN", "https://www.linkedin.com/sharing/share-offsite/?url="}}) {
            mvc.perform(post("/eventos/1/compartir").param("canal", caso[0])
                            .with(authentication(Datos.gamer())).with(csrf()))
                    .andExpect(header().string("Location", startsWith(caso[1])));
        }
    }
}
