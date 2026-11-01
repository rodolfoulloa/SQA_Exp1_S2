package com.duoc.pixelmeet.api.dto;

import java.time.Instant;
import java.util.List;

/** Respuesta del login: token JWT y datos basicos del usuario autenticado. */
public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        String username,
        String nombre,
        List<String> roles) {
}
