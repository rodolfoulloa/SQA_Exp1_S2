package com.duoc.pixelmeet.web.controller;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Arma los enlaces para compartir en redes sociales. Los destinos son fijos (lista blanca): el usuario solo
 * elige el canal, nunca la URL de destino, por lo que no hay redireccion abierta. Los parametros se codifican.
 */
final class RedesSociales {

    /** Canales de redes sociales permitidos y su nombre visible. */
    static final Map<String, String> CANALES;

    static {
        Map<String, String> canales = new LinkedHashMap<>();
        canales.put("WHATSAPP", "WhatsApp");
        canales.put("X", "X (Twitter)");
        canales.put("FACEBOOK", "Facebook");
        canales.put("LINKEDIN", "LinkedIn");
        CANALES = Collections.unmodifiableMap(canales);
    }

    private RedesSociales() {
    }

    static String enlace(String canal, String urlEvento, String texto) {
        return switch (canal) {
            case "X" -> UriComponentsBuilder.fromUriString("https://twitter.com/intent/tweet")
                    .queryParam("text", texto).queryParam("url", urlEvento).encode().build().toUriString();
            case "FACEBOOK" -> UriComponentsBuilder.fromUriString("https://www.facebook.com/sharer/sharer.php")
                    .queryParam("u", urlEvento).encode().build().toUriString();
            case "WHATSAPP" -> UriComponentsBuilder.fromUriString("https://wa.me/")
                    .queryParam("text", texto + " " + urlEvento).encode().build().toUriString();
            case "LINKEDIN" -> UriComponentsBuilder.fromUriString("https://www.linkedin.com/sharing/share-offsite/")
                    .queryParam("url", urlEvento).encode().build().toUriString();
            default -> throw new IllegalArgumentException("Canal no permitido");
        };
    }
}
