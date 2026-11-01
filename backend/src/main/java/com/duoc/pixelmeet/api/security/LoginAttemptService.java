package com.duoc.pixelmeet.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteccion basica contra fuerza bruta (OWASP A07): despues de N intentos fallidos
 * para un mismo usuario se bloquea el login durante unos minutos.
 */
@Service
public class LoginAttemptService {

    private final int maxIntentos;
    private final Duration bloqueo;
    private final Clock clock;
    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(@Value("${app.login.max-intentos:5}") int maxIntentos,
                               @Value("${app.login.bloqueo-minutos:5}") long bloqueoMinutos) {
        this(maxIntentos, Duration.ofMinutes(bloqueoMinutos), Clock.systemUTC());
    }

    LoginAttemptService(int maxIntentos, Duration bloqueo, Clock clock) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.clock = clock;
    }

    public boolean estaBloqueado(String usuario) {
        Registro r = registros.get(clave(usuario));
        if (r == null || r.bloqueadoHasta == null) {
            return false;
        }
        if (Instant.now(clock).isAfter(r.bloqueadoHasta)) {
            registros.remove(clave(usuario));
            return false;
        }
        return true;
    }

    public void falla(String usuario) {
        registros.compute(clave(usuario), (k, r) -> {
            Registro nuevo = r == null ? new Registro() : r;
            nuevo.fallos++;
            if (nuevo.fallos >= maxIntentos) {
                nuevo.bloqueadoHasta = Instant.now(clock).plus(bloqueo);
            }
            return nuevo;
        });
    }

    public void exito(String usuario) {
        registros.remove(clave(usuario));
    }

    private static String clave(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Registro {
        private int fallos;
        private Instant bloqueadoHasta;
    }
}
