package com.duoc.pixelmeet.web.config;

import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * La pauta exige que el sitio se publique en http://[IP]/recetas, por eso la aplicacion vive bajo ese
 * context path. Para que la raiz no quede en 404, cualquier peticion fuera de /recetas se redirige
 * a la misma ruta dentro del sitio: "/" -> "/recetas/", "/eventos?x=1" -> "/recetas/eventos?x=1".
 * <p>
 * Se implementa como Valve de Tomcat porque esas peticiones no llegan a la aplicacion (estan fuera
 * de su context path). La redireccion es relativa, asi conserva el esquema (http o https).
 */
@Configuration
public class RedireccionRaizConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> redireccionRaiz(
            @Value("${server.servlet.context-path:}") String contextPath) {
        return factory -> {
            if (contextPath != null && !contextPath.isBlank() && !"/".equals(contextPath)) {
                factory.addEngineValves(new RedireccionValve(contextPath));
            }
        };
    }

    static final class RedireccionValve extends ValveBase {

        private final String contextPath;

        RedireccionValve(String contextPath) {
            super(true);
            this.contextPath = contextPath.endsWith("/")
                    ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
        }

        @Override
        public void invoke(Request request, Response response) throws IOException, ServletException {
            String uri = request.getDecodedRequestURI();
            if (uri == null || uri.equals(contextPath) || uri.startsWith(contextPath + "/")) {
                getNext().invoke(request, response);
                return;
            }
            // Se usa la URI original (codificada) para no alterar caracteres especiales.
            String destino = contextPath + request.getRequestURI();
            if (destino.startsWith(contextPath + "//")) {
                // Evita redirecciones abiertas del tipo "//otro-sitio.com".
                destino = contextPath + "/";
            }
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                destino += "?" + query;
            }
            response.setStatus(302);
            response.setHeader("Location", destino);
            response.setContentLength(0);
        }
    }
}
