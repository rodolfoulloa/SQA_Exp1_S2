package com.duoc.pixelmeet.web.client;

import com.duoc.pixelmeet.web.client.ApiDtos.ApiError;
import com.duoc.pixelmeet.web.client.ApiDtos.EventoDetalle;
import com.duoc.pixelmeet.web.client.ApiDtos.EventoResumen;
import com.duoc.pixelmeet.web.client.ApiDtos.Filtros;
import com.duoc.pixelmeet.web.client.ApiDtos.Home;
import com.duoc.pixelmeet.web.client.ApiDtos.LoginRequest;
import com.duoc.pixelmeet.web.client.ApiDtos.LoginResponse;
import com.duoc.pixelmeet.web.client.ApiDtos.Perfil;
import com.duoc.pixelmeet.web.client.ApiDtos.PerfilRequest;
import com.duoc.pixelmeet.web.client.ApiDtos.RegistroRequest;
import com.duoc.pixelmeet.web.client.ApiDtos.Usuario;
import com.duoc.pixelmeet.web.config.ApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Cliente HTTP de la API de PixelMeet.
 * El token JWT se recibe como parametro y se envia en el header Authorization: Bearer;
 * nunca se entrega al navegador.
 */
@Component
public class PixelMeetApiClient {

    private static final Logger log = LoggerFactory.getLogger(PixelMeetApiClient.class);

    private final RestClient http;

    public PixelMeetApiClient(ApiProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeoutMs());
        factory.setReadTimeout(props.readTimeoutMs());
        DefaultUriBuilderFactory uris = new DefaultUriBuilderFactory(props.baseUrl());
        uris.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);
        this.http = RestClient.builder()
                .uriBuilderFactory(uris)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ---------------- Publicas ----------------

    public LoginResponse login(String username, String password) {
        return ejecutar(http.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(username, password)), LoginResponse.class);
    }

    public Usuario registrar(RegistroRequest req) {
        return ejecutar(http.post().uri("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .body(req), Usuario.class);
    }

    public Home home() {
        return ejecutar(http.get().uri("/api/home"), Home.class);
    }

    public Filtros filtros() {
        return ejecutar(http.get().uri("/api/eventos/filtros"), Filtros.class);
    }

    public List<EventoResumen> buscar(BusquedaForm f) {
        Map<String, Object> filtros = new LinkedHashMap<>();
        filtros.put("nombre", f.getNombre());
        filtros.put("videojuego", f.getVideojuego());
        filtros.put("tipo", f.getTipo());
        filtros.put("pais", f.getPais());
        filtros.put("ciudad", f.getCiudad());
        filtros.put("desde", f.getDesde());
        filtros.put("hasta", f.getHasta());
        filtros.put("modalidad", f.getModalidad());
        filtros.values().removeIf(Objects::isNull);

        // Cada valor va como variable de plantilla: se codifica completo (incluidos & = + #),
        // asi el usuario no puede inyectar parametros adicionales en la URL hacia la API.
        Function<UriBuilder, URI> uri = b -> {
            b.path("/api/eventos");
            filtros.keySet().forEach(k -> b.queryParam(k, "{" + k + "}"));
            return b.build(filtros);
        };
        return ejecutar(http.get().uri(uri), new ParameterizedTypeReference<List<EventoResumen>>() { });
    }

    // ---------------- Privadas (requieren JWT) ----------------

    public EventoDetalle detalle(long id, String token) {
        return ejecutar(http.get().uri("/api/eventos/{id}", id).headers(h -> h.setBearerAuth(token)),
                EventoDetalle.class);
    }

    public Perfil perfil(String token) {
        return ejecutar(http.get().uri("/api/perfil").headers(h -> h.setBearerAuth(token)), Perfil.class);
    }

    public Perfil actualizarPerfil(PerfilRequest req, String token) {
        return ejecutar(http.put().uri("/api/perfil").headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON).body(req), Perfil.class);
    }

    public List<Usuario> usuarios(String token) {
        return ejecutar(http.get().uri("/api/admin/usuarios").headers(h -> h.setBearerAuth(token)),
                new ParameterizedTypeReference<List<Usuario>>() { });
    }

    // ---------------- Infraestructura ----------------

    private <T> T ejecutar(RestClient.RequestHeadersSpec<?> spec, Class<T> tipo) {
        return ejecutar(spec, res -> res.bodyTo(tipo));
    }

    private <T> T ejecutar(RestClient.RequestHeadersSpec<?> spec, ParameterizedTypeReference<T> tipo) {
        return ejecutar(spec, res -> res.bodyTo(tipo));
    }

    private interface Lector<T> {
        T leer(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse res) throws java.io.IOException;
    }

    private <T> T ejecutar(RestClient.RequestHeadersSpec<?> spec, Lector<T> lector) {
        try {
            return spec.exchange((req, res) -> {
                HttpStatusCode status = res.getStatusCode();
                if (status.is2xxSuccessful()) {
                    return lector.leer(res);
                }
                throw traducir(status, leerError(res));
            });
        } catch (ApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("La API no responde: {}", ex.getMessage());
            throw new ApiException(ApiException.Tipo.NO_DISPONIBLE, "El servicio no está disponible", ex);
        } catch (RestClientException ex) {
            if (ex.getCause() instanceof ApiException api) {
                throw api;
            }
            log.error("Error al llamar a la API", ex);
            throw new ApiException(ApiException.Tipo.NO_DISPONIBLE, "El servicio no está disponible", ex);
        }
    }

    private static ApiError leerError(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse res) {
        try {
            return res.bodyTo(ApiError.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private static ApiException traducir(HttpStatusCode status, ApiError err) {
        String mensaje = err != null && err.mensaje() != null ? err.mensaje() : "Error " + status.value();
        ApiException.Tipo tipo = switch (status.value()) {
            case 400, 409 -> ApiException.Tipo.VALIDACION;
            case 401 -> ApiException.Tipo.NO_AUTENTICADO;
            case 403 -> ApiException.Tipo.PROHIBIDO;
            case 404 -> ApiException.Tipo.NO_ENCONTRADO;
            case 429 -> ApiException.Tipo.DEMASIADOS_INTENTOS;
            default -> ApiException.Tipo.NO_DISPONIBLE;
        };
        return new ApiException(tipo, mensaje, err == null ? null : err.campos());
    }
}
