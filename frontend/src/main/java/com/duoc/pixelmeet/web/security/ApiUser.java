package com.duoc.pixelmeet.web.security;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

/**
 * Usuario autenticado en el frontend. Se guarda en la sesion HTTP del servidor junto con el JWT;
 * el navegador solo recibe la cookie de sesion (HttpOnly, Secure), nunca el token.
 */
public record ApiUser(String username, String nombre, List<String> roles, String token, Instant expiraEn)
        implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return username;
    }

    public boolean tokenVencido() {
        return expiraEn != null && Instant.now().isAfter(expiraEn);
    }

    /** No se imprime el token en logs. */
    @Override
    public String toString() {
        return "ApiUser[" + username + ", roles=" + roles + "]";
    }
}
