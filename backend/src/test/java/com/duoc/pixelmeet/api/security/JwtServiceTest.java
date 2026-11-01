package com.duoc.pixelmeet.api.security;

import com.duoc.pixelmeet.api.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "dGVzdC1zZWNyZXQtcGl4ZWxtZWV0LWFwaS1zb2xvLXBhcmEtcHJ1ZWJhcy0yMDI2";

    @Test
    void generaYValidaToken() {
        JwtService jwt = new JwtService(new JwtProperties(SECRETO, 60, "pixelmeet-api"));
        var token = jwt.generar(User.withUsername("gamer1").password("x").roles("USER").build());
        assertThat(token.token().split("\\.")).hasSize(3);
        assertThat(jwt.validar(token.token())).contains("gamer1");
    }

    @Test
    void tokenDeOtraInstanciaConOtraClaveNoEsValido() {
        JwtService a = new JwtService(new JwtProperties(SECRETO, 60, "pixelmeet-api"));
        JwtService b = new JwtService(new JwtProperties(null, 60, "pixelmeet-api"));
        var token = a.generar(User.withUsername("gamer1").password("x").roles("USER").build());
        assertThat(b.validar(token.token())).isEmpty();
    }

    @Test
    void rechazaClavesCortas() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("Y29ydGE=", 60, "pixelmeet-api")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void textoQueNoEsJwtNoEsValido() {
        JwtService jwt = new JwtService(new JwtProperties(SECRETO, 60, "pixelmeet-api"));
        assertThat(jwt.validar("no-es-un-token")).isEmpty();
        assertThat(jwt.validar("")).isEmpty();
    }
}
