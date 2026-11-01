package com.duoc.pixelmeet.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso al usuario autenticado y a su JWT. */
public final class SesionUsuario {

    private SesionUsuario() {
    }

    public static ApiUser actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof ApiUser user) {
            return user;
        }
        throw new IllegalStateException("No hay un usuario autenticado");
    }

    public static String token() {
        return actual().token();
    }
}
