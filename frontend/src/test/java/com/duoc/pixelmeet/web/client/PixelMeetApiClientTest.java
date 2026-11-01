package com.duoc.pixelmeet.web.client;

import com.duoc.pixelmeet.web.config.ApiProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba el cliente contra un servidor HTTP local que simula la API. */
class PixelMeetApiClientTest {

    private HttpServer server;
    private PixelMeetApiClient client;
    private final AtomicReference<String> ultimaQuery = new AtomicReference<>();
    private final AtomicReference<String> ultimoAuth = new AtomicReference<>();

    @BeforeEach
    void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/eventos", ex -> {
            ultimaQuery.set(ex.getRequestURI().getRawQuery());
            ultimoAuth.set(ex.getRequestHeaders().getFirst("Authorization"));
            String path = ex.getRequestURI().getPath();
            if (path.equals("/api/eventos/1") && ultimoAuth.get() == null) {
                responder(ex, 401, "{\"estado\":401,\"mensaje\":\"Debes iniciar sesión\"}");
            } else if (path.equals("/api/eventos/1")) {
                responder(ex, 200, "{\"id\":1,\"nombre\":\"Copa\",\"modalidad\":\"ONLINE\",\"fechaHora\":"
                        + "\"2026-11-21T10:00:00\",\"fotos\":[\"a.svg\"],\"interesados\":5,\"destacado\":false}");
            } else if (path.equals("/api/eventos/2")) {
                responder(ex, 404, "{\"estado\":404,\"mensaje\":\"Evento no encontrado\"}");
            } else {
                responder(ex, 200, "[]");
            }
        });
        server.createContext("/api/auth/registro", ex -> responder(ex, 409,
                "{\"estado\":409,\"mensaje\":\"El correo ya está registrado\","
                        + "\"campos\":{\"correo\":\"El correo ya está registrado\"}}"));
        server.createContext("/api/auth/login", ex -> responder(ex, 429,
                "{\"estado\":429,\"mensaje\":\"Demasiados intentos\"}"));
        server.start();
        client = new PixelMeetApiClient(new ApiProperties(
                "http://127.0.0.1:" + server.getAddress().getPort(), 1000, 2000));
    }

    @AfterEach
    void detener() {
        server.stop(0);
    }

    private static void responder(com.sun.net.httpserver.HttpExchange ex, int status, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    @Test
    void losFiltrosSeCodificanYNoPermitenInyectarParametros() {
        BusquedaForm f = new BusquedaForm();
        f.setCiudad("Santiago&modalidad=ONLINE");
        f.setNombre("copa #1 + 50%");
        client.buscar(f);
        String q = ultimaQuery.get();
        assertThat(q).contains("ciudad=Santiago%26modalidad%3DONLINE");
        assertThat(q).doesNotContain("&modalidad=");
        assertThat(q).contains("nombre=copa%20%231%20%2B%2050%25");
    }

    @Test
    void elJwtViajaEnElHeaderAuthorization() {
        var detalle = client.detalle(1, "abc.def.ghi");
        assertThat(ultimoAuth.get()).isEqualTo("Bearer abc.def.ghi");
        assertThat(detalle.isOnline()).isTrue();
        assertThat(detalle.fotos()).containsExactly("a.svg");
    }

    @Test
    void lasRespuestasDeErrorSeTraducen() {
        assertThatThrownBy(() -> client.detalle(2, "t"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getTipo()).isEqualTo(ApiException.Tipo.NO_ENCONTRADO));
        assertThatThrownBy(() -> client.registrar(new ApiDtos.RegistroRequest("a", "b@c.cl", "Clave1234")))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getTipo()).isEqualTo(ApiException.Tipo.VALIDACION);
                    assertThat(e.getCampos()).containsKey("correo");
                });
        assertThatThrownBy(() -> client.login("x", "y"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getTipo()).isEqualTo(ApiException.Tipo.DEMASIADOS_INTENTOS));
    }

    @Test
    void apiSinRespuestaSeInformaComoNoDisponible() {
        server.stop(0);
        assertThatThrownBy(() -> client.home())
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getTipo()).isEqualTo(ApiException.Tipo.NO_DISPONIBLE));
    }
}
