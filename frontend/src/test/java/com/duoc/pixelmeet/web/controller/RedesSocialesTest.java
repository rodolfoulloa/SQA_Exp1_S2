package com.duoc.pixelmeet.web.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedesSocialesTest {

    @Test
    void urlPublicaSinBarrasFinales() {
        assertThat(ComunidadController.sinBarraFinal("http://1.2.3.4/eventos//")).isEqualTo("http://1.2.3.4/eventos");
        assertThat(ComunidadController.sinBarraFinal("http://1.2.3.4/eventos")).isEqualTo("http://1.2.3.4/eventos");
        assertThat(ComunidadController.sinBarraFinal("///")).isEmpty();
    }

    @Test
    void canalFueraDeLaListaBlancaSeRechaza() {
        assertThatThrownBy(() -> RedesSociales.enlace("https://evil.example", "http://x/eventos/1", "hola"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elTextoYLaUrlSeCodificanEnElEnlace() {
        assertThat(RedesSociales.enlace("X", "http://x/eventos/1", "a&b=c"))
                .startsWith("https://twitter.com/intent/tweet?")
                .contains("text=a%26b%3Dc")
                .doesNotContain("&b=c");
    }
}
