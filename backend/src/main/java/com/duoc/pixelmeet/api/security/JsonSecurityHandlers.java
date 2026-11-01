package com.duoc.pixelmeet.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Respuestas JSON para 401 (sin token o token invalido) y 403 (sin el rol requerido). */
public final class JsonSecurityHandlers {

    private JsonSecurityHandlers() {
    }

    public static AuthenticationEntryPoint entryPoint() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            res.setHeader("WWW-Authenticate", "Bearer");
            escribir(res, req, 401, "Unauthorized", "Debes iniciar sesión para acceder a este recurso");
        };
    }

    public static AccessDeniedHandler accessDenied() {
        return (HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) ->
                escribir(res, req, 403, "Forbidden", "No tienes permisos para este recurso");
    }

    private static void escribir(HttpServletResponse res, HttpServletRequest req, int estado, String error,
                                 String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Los textos son constantes; solo la ruta proviene de la peticion y se escapa.
        String json = "{\"fecha\":\"" + Instant.now() + "\",\"estado\":" + estado + ",\"error\":\"" + error
                + "\",\"mensaje\":\"" + mensaje + "\",\"ruta\":\"" + escapar(req.getRequestURI()) + "\"}";
        res.getWriter().write(json);
    }

    private static String escapar(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
