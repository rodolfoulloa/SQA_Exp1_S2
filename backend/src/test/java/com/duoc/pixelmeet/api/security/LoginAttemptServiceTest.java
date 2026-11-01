package com.duoc.pixelmeet.api.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    /** Reloj manipulable para simular el paso del tiempo. */
    private static final class RelojFalso extends Clock {
        private Instant ahora = Instant.parse("2026-11-01T12:00:00Z");

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
        void avanzar(Duration d) { ahora = ahora.plus(d); }
    }

    @Test
    void bloqueaAlLlegarAlMaximoYSeLiberaDespuesDelTiempo() {
        RelojFalso reloj = new RelojFalso();
        LoginAttemptService s = new LoginAttemptService(3, Duration.ofMinutes(5), reloj);

        s.falla("Gamer1");
        s.falla("gamer1");
        assertThat(s.estaBloqueado("gamer1")).isFalse();
        s.falla("GAMER1");
        assertThat(s.estaBloqueado("gamer1")).isTrue();

        reloj.avanzar(Duration.ofMinutes(6));
        assertThat(s.estaBloqueado("gamer1")).isFalse();
    }

    @Test
    void unLoginExitosoReiniciaElContador() {
        LoginAttemptService s = new LoginAttemptService(3, Duration.ofMinutes(5), new RelojFalso());
        s.falla("gamer2");
        s.falla("gamer2");
        s.exito("gamer2");
        s.falla("gamer2");
        assertThat(s.estaBloqueado("gamer2")).isFalse();
    }
}
