package com.duoc.pixelmeet.web;

import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Prueba con Tomcat real: las rutas fuera de /recetas redirigen al sitio. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.servlet.context-path=/recetas")
class RedireccionRaizTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private PixelMeetApiClient api;

    private HttpResponse<String> get(String ruta) throws Exception {
        HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + ruta)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void laRaizRedirigeARecetas() throws Exception {
        HttpResponse<String> r = get("/");
        assertThat(r.statusCode()).isEqualTo(302);
        assertThat(r.headers().firstValue("Location")).contains("/recetas/");
    }

    @Test
    void otrasRutasSeRedirigenConservandoRutaYParametros() throws Exception {
        HttpResponse<String> r = get("/eventos?modalidad=ONLINE");
        assertThat(r.statusCode()).isEqualTo(302);
        assertThat(r.headers().firstValue("Location")).contains("/recetas/eventos?modalidad=ONLINE");
    }

    @Test
    void noPermiteRedireccionesAOtroSitio() throws Exception {
        HttpResponse<String> r = get("//evil.example.com");
        assertThat(r.headers().firstValue("Location").orElse("")).isEqualTo("/recetas/");
    }

    @Test
    void lasRutasDelSitioNoSeRedirigen() throws Exception {
        when(api.home()).thenReturn(Datos.home());
        assertThat(get("/recetas/home").statusCode()).isEqualTo(200);
    }
}
