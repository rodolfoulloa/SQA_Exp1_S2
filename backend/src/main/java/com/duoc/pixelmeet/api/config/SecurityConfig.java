package com.duoc.pixelmeet.api.config;

import com.duoc.pixelmeet.api.security.JsonSecurityHandlers;
import com.duoc.pixelmeet.api.security.JwtAuthenticationFilter;
import com.duoc.pixelmeet.api.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.CrossOriginEmbedderPolicyHeaderWriter;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter;

/**
 * Seguridad de la API.
 * <ul>
 *   <li>Publicas: GET /api/auth/csrf, POST /api/auth/login, POST /api/auth/registro, GET /api/home,
 *       GET /api/eventos y GET /api/eventos/filtros.</li>
 *   <li>Privadas (JWT): GET /api/eventos/{id}, POST /api/eventos, /api/auth/me y /api/perfil.</li>
 *   <li>Solo ADMIN: /api/admin/**.</li>
 *   <li>Todo lo demas se rechaza.</li>
 * </ul>
 * CSRF activo en toda peticion que modifica datos (POST, PUT, DELETE...), con el patron "double submit
 * cookie": el cliente obtiene un token en GET /api/auth/csrf (cookie XSRF-TOKEN + valor en el cuerpo) y lo
 * devuelve en la cookie y en el header X-XSRF-TOKEN. Un sitio externo no puede leer esa cookie ni
 * agregar el header, por lo que no puede forjar peticiones. La API sigue siendo stateless.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtService jwtService,
                                              UserDetailsService userDetailsService) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                // Token sin enmascarar: el cliente es el servidor frontend, no un navegador (no aplica BREACH).
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/home", "/api/eventos", "/api/eventos/filtros",
                        "/api/auth/csrf").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/eventos/*", "/api/eventos/*/comunidad",
                        "/api/eventos/*/media/*/contenido", "/api/auth/me").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/eventos", "/api/eventos/*/fotos", "/api/eventos/*/videos",
                        "/api/eventos/*/comentarios", "/api/eventos/*/compartir").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/eventos/*/valoracion").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/eventos/*/media/*", "/api/eventos/*/comentarios/*")
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/perfil").hasAnyRole("USER", "ADMIN")
                .anyRequest().denyAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(JsonSecurityHandlers.entryPoint())
                .accessDeniedHandler(JsonSecurityHandlers.accessDenied())
            )
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                .frameOptions(f -> f.deny())
                // Aislamiento entre origenes (ZAP: Cross-Origin-*-Policy, mitigacion de Spectre)
                .crossOriginOpenerPolicy(coop -> coop.policy(
                        CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN))
                .crossOriginEmbedderPolicy(coep -> coep.policy(
                        CrossOriginEmbedderPolicyHeaderWriter.CrossOriginEmbedderPolicy.REQUIRE_CORP))
                .crossOriginResourcePolicy(corp -> corp.policy(
                        CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy.SAME_ORIGIN))
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Token CSRF en cookie (no en sesion, la API es stateless). HttpOnly, Secure y SameSite=Strict. */
    static CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = new CookieCsrfTokenRepository();
        repo.setCookieCustomizer(c -> c.httpOnly(true).secure(true).sameSite("Strict"));
        return repo;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
