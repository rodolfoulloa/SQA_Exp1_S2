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

/**
 * Seguridad de la API.
 * <ul>
 *   <li>Publicas: POST /api/auth/login, POST /api/auth/registro, GET /api/home, GET /api/eventos y
 *       GET /api/eventos/filtros.</li>
 *   <li>Privadas (JWT): GET /api/eventos/{id}, /api/auth/me y /api/perfil.</li>
 *   <li>Solo ADMIN: /api/admin/**.</li>
 *   <li>Todo lo demas se rechaza.</li>
 * </ul>
 * API stateless: no hay sesion HTTP ni cookies, por eso CSRF no aplica y se desactiva.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtService jwtService,
                                              UserDetailsService userDetailsService) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/home", "/api/eventos", "/api/eventos/filtros").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/eventos/*", "/api/auth/me").hasAnyRole("USER", "ADMIN")
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
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
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
