package com.duoc.pixelmeet.api.security;

import com.duoc.pixelmeet.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Emite y valida tokens JWT firmados con HMAC-SHA256.
 * El token incluye: sub (usuario), roles, iss, iat, exp y jti.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        if (props.secret() == null || props.secret().isBlank()) {
            // Sin clave configurada se genera una aleatoria: es seguro, pero los tokens
            // dejan de servir al reiniciar. En Docker la clave llega por JWT_SECRET.
            log.warn("JWT_SECRET no configurado: se genero una clave aleatoria temporal");
            this.key = Jwts.SIG.HS256.key().build();
        } else {
            byte[] bytes = Decoders.BASE64.decode(props.secret());
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 256 bits (32 bytes en Base64)");
            }
            this.key = Keys.hmacShaKeyFor(bytes);
        }
    }

    public TokenEmitido generar(UserDetails usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(props.expirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.issuer())
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new TokenEmitido(token, expira);
    }

    /**
     * Valida firma, expiracion y emisor.
     *
     * @return el nombre de usuario (sub) si el token es valido
     */
    public Optional<String> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(props.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
