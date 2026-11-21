package com.duoc.pixelmeet.api.service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reconoce enlaces de video de una lista blanca (YouTube y Vimeo, solo https) y extrae su identificador.
 * Solo se guarda plataforma + id: asi no se almacenan ni se muestran URLs arbitrarias (javascript:,
 * sitios de phishing, parametros extra).
 */
public final class VideoEnlace {

    public record Video(String plataforma, String id) {
    }

    private record Regla(String plataforma, Pattern patron) {
    }

    private static final List<Regla> REGLAS = List.of(
            new Regla("YOUTUBE", Pattern.compile(
                    "^https://(?:www\\.|m\\.)?youtube\\.com/watch\\?(?:[^#\\s]*&)?v=([A-Za-z0-9_-]{11})(?:[&#][^\\s]*)?$")),
            new Regla("YOUTUBE", Pattern.compile(
                    "^https://(?:www\\.)?youtube\\.com/(?:shorts|embed)/([A-Za-z0-9_-]{11})(?:[?#][^\\s]*)?$")),
            new Regla("YOUTUBE", Pattern.compile(
                    "^https://youtu\\.be/([A-Za-z0-9_-]{11})(?:[?#][^\\s]*)?$")),
            new Regla("VIMEO", Pattern.compile(
                    "^https://(?:www\\.)?vimeo\\.com/(\\d{6,12})(?:[?#][^\\s]*)?$")));

    private VideoEnlace() {
    }

    public static Optional<Video> reconocer(String url) {
        if (url == null) {
            return Optional.empty();
        }
        String limpia = url.trim();
        for (Regla regla : REGLAS) {
            Matcher m = regla.patron().matcher(limpia);
            if (m.matches()) {
                return Optional.of(new Video(regla.plataforma(), m.group(1)));
            }
        }
        return Optional.empty();
    }
}
