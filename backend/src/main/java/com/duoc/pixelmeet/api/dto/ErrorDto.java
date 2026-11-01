package com.duoc.pixelmeet.api.dto;

import java.time.Instant;
import java.util.Map;

/** Formato unico de error: no expone trazas ni detalles internos. */
public record ErrorDto(Instant fecha, int estado, String error, String mensaje, String ruta,
                       Map<String, String> campos) {

    public static ErrorDto de(int estado, String error, String mensaje, String ruta) {
        return new ErrorDto(Instant.now(), estado, error, mensaje, ruta, null);
    }
}
