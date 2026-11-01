package com.duoc.pixelmeet.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Conexion con la API backend (prefijo app.api).
 *
 * @param baseUrl            URL base de la API, por ejemplo http://backend:8080
 * @param connectTimeoutMs   tiempo maximo para conectar
 * @param readTimeoutMs      tiempo maximo de espera de la respuesta
 */
@ConfigurationProperties(prefix = "app.api")
public record ApiProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {

    public ApiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        if (connectTimeoutMs <= 0) {
            connectTimeoutMs = 3000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 10000;
        }
    }
}
