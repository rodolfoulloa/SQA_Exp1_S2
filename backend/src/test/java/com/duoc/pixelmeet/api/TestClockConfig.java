package com.duoc.pixelmeet.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** Fija "hoy" en 01-11-2026 para que las pruebas no dependan de la fecha real. */
@TestConfiguration
public class TestClockConfig {

    @Bean
    public Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneId.of("America/Santiago"));
    }
}
