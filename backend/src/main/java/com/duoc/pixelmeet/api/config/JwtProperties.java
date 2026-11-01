package com.duoc.pixelmeet.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del JWT (prefijo app.jwt).
 *
 * @param secret            clave HMAC en Base64 (minimo 256 bits). Se entrega por la variable de entorno JWT_SECRET.
 * @param expirationMinutes minutos de validez del token
 * @param issuer            emisor que se escribe y se valida en el token
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes, String issuer) {

    public JwtProperties {
        if (expirationMinutes <= 0) {
            expirationMinutes = 60;
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "pixelmeet-api";
        }
    }
}
