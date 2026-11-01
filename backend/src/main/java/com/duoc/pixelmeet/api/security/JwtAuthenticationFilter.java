package com.duoc.pixelmeet.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee el header "Authorization: Bearer &lt;token&gt;", valida el JWT y deja al usuario
 * autenticado en el SecurityContext. Si el token no es valido la peticion sigue como anonima
 * y Spring Security responde 401 en las rutas privadas.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIJO.length()).trim();
            jwtService.validar(token).ifPresent(username -> autenticar(username, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String username, HttpServletRequest request) {
        try {
            // Se recarga el usuario desde la BD: si fue desactivado, su token deja de servir.
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (!user.isEnabled()) {
                return;
            }
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
        }
    }
}
