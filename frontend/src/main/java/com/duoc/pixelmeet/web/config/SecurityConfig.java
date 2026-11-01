package com.duoc.pixelmeet.web.config;

import com.duoc.pixelmeet.web.security.ApiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.Map;

/**
 * Seguridad del frontend.
 * <ul>
 *   <li>Publicas: inicio, busqueda de eventos, login, registro y recursos estaticos.</li>
 *   <li>Privadas: detalle de eventos y perfil. Solo ADMIN: /admin/**.</li>
 * </ul>
 * La autenticacion se delega en la API (ApiAuthenticationProvider), que devuelve un JWT.
 * Con app.https.enabled=true toda peticion HTTP se redirige a HTTPS y se envia HSTS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String[] RUTAS_PUBLICAS = {
            "/", "/home", "/eventos", "/login", "/registro", "/error",
            "/images/**", "/css/**", "/webjars/**", "/favicon.ico"
    };

    private static final String CSP =
            "default-src 'self'; "
            + "img-src 'self' data:; "
            + "style-src 'self'; "
            + "script-src 'self'; "
            + "font-src 'self'; "
            + "connect-src 'self'; "
            + "form-action 'self'; "
            + "frame-ancestors 'none'; "
            + "base-uri 'self'; "
            + "object-src 'none'";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiAuthenticationProvider apiProvider,
                                                   @Value("${app.https.enabled:false}") boolean https,
                                                   @Value("${app.https.http-port:8080}") int httpPort,
                                                   @Value("${app.https.https-port:8443}") int httpsPort)
            throws Exception {
        http
            .authenticationProvider(apiProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(RUTAS_PUBLICAS).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/eventos/*", "/perfil").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", false)
                .failureHandler(manejadorFallos())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("PIXELMEET_SESSION")
                .permitAll()
            )
            // CSRF activo (por defecto): Thymeleaf agrega el token en cada formulario th:action.
            .csrf(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.changeSessionId())
                .maximumSessions(3)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(ref -> ref.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicyHeader(p -> p.policy("camera=(), microphone=(), geolocation=()"))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
            );

        if (https) {
            // Puertos internos del contenedor y puertos publicados por Docker (80 -> 443).
            PortMapperImpl portMapper = new PortMapperImpl();
            portMapper.setPortMappings(Map.of(
                    String.valueOf(httpPort), String.valueOf(httpsPort),
                    "80", "443"));
            http.portMapper(pm -> pm.portMapper(portMapper))
                .redirectToHttps(Customizer.withDefaults());
        }
        return http.build();
    }

    /** Traduce el motivo del fallo a un parametro que la vista muestra con un mensaje fijo. */
    private AuthenticationFailureHandler manejadorFallos() {
        return (request, response, ex) -> {
            String motivo = "error";
            if (ex instanceof LockedException) {
                motivo = "bloqueado";
            } else if (ex instanceof AuthenticationServiceException) {
                motivo = "servicio";
            }
            response.sendRedirect(request.getContextPath() + "/login?" + motivo);
        };
    }
}
