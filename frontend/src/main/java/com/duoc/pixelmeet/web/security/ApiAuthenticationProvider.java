package com.duoc.pixelmeet.web.security;

import com.duoc.pixelmeet.web.client.ApiDtos.LoginResponse;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Autentica contra la API: envia usuario y clave a POST /api/auth/login y, si son correctos,
 * construye la autenticacion con el JWT recibido y los roles del usuario.
 */
@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    private final PixelMeetApiClient api;

    public ApiAuthenticationProvider(PixelMeetApiClient api) {
        this.api = api;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String usuario = authentication.getName() == null ? "" : authentication.getName().trim();
        String clave = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();
        if (usuario.isEmpty() || clave.isEmpty()) {
            throw new BadCredentialsException("Credenciales incompletas");
        }
        try {
            LoginResponse r = api.login(usuario, clave);
            ApiUser principal = new ApiUser(r.username(), r.nombre(), r.roles(), r.token(), r.expiraEn());
            var authorities = r.roles().stream().map(rol -> new SimpleGrantedAuthority("ROLE_" + rol)).toList();
            // La clave no se conserva (credentials = null).
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
        } catch (ApiException ex) {
            throw switch (ex.getTipo()) {
                case NO_AUTENTICADO, VALIDACION -> new BadCredentialsException("Credenciales inválidas");
                case DEMASIADOS_INTENTOS -> new LockedException(ex.getMessage());
                default -> new AuthenticationServiceException("Servicio no disponible", ex);
            };
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
