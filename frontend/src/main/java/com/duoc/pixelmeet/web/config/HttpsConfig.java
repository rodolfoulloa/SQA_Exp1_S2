package com.duoc.pixelmeet.web.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Con HTTPS activo, el conector principal de Tomcat atiende TLS (server.ssl.*) y se agrega
 * un segundo conector HTTP cuya unica funcion es recibir http://... y redirigir a https://...
 * (la redireccion la hace Spring Security con redirectToHttps).
 */
@Configuration
@ConditionalOnProperty(name = "app.https.enabled", havingValue = "true")
public class HttpsConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> conectorHttp(
            @Value("${app.https.http-port:8080}") int httpPort,
            @Value("${app.https.https-port:8443}") int httpsPort) {
        return factory -> {
            Connector http = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            http.setScheme("http");
            http.setPort(httpPort);
            http.setSecure(false);
            http.setRedirectPort(httpsPort);
            factory.addAdditionalConnectors(http);
        };
    }
}
