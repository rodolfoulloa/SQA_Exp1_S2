package com.duoc.pixelmeet.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PerfilApiTest extends ApiTestBase {

    @Test
    void cadaUsuarioVeSuPropioPerfil() throws Exception {
        mvc.perform(get("/api/perfil").header("Authorization", bearer(login("gamer2", "Gamer123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuenta").value("gamer2"))
                .andExpect(jsonPath("$.nombreUsuario").value("diegomz"));
    }

    @Test
    void actualizaPerfilFiltrandoValoresNoPermitidos() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(put("/api/perfil").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"cami_pro\",\"avatar\":\"../../etc/passwd\","
                                + "\"juegos\":[\"Valorant\",\"<script>alert(1)</script>\",\"Valorant\"],"
                                + "\"notifCorreo\":false,\"notifPush\":true,\"notifNuevosEventos\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("cami_pro"))
                .andExpect(jsonPath("$.avatar").value("avatar-1"))
                .andExpect(jsonPath("$.juegos", contains("Valorant")))
                .andExpect(jsonPath("$.notifPush").value(true));
    }

    @Test
    void aliasInvalidoDevuelve400() throws Exception {
        String token = login("gamer1", "Gamer123!");
        mvc.perform(put("/api/perfil").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"<b>x</b>\",\"avatar\":\"avatar-1\",\"juegos\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombreUsuario").exists());
    }

    @Test
    void aliasDeOtroUsuarioDevuelve409() throws Exception {
        String token = login("admin", "Admin123!");
        mvc.perform(put("/api/perfil").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"diegomz\",\"avatar\":\"avatar-1\",\"juegos\":[]}"))
                .andExpect(status().isConflict());
    }
}
