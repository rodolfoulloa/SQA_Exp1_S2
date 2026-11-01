package com.duoc.pixelmeet.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventoBusquedaApiTest extends ApiTestBase {

    @Test
    void sinFiltrosDevuelveTodos() throws Exception {
        mvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void filtraPorNombreParcialSinDistinguirMayusculas() throws Exception {
        mvc.perform(get("/api/eventos").param("nombre", "copa"))
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[*].nombre", everyItem(containsStringIgnoringCase("copa"))));
    }

    @Test
    void filtraPorVideojuego() throws Exception {
        mvc.perform(get("/api/eventos").param("videojuego", "Valorant"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].videojuego").value("Valorant"));
    }

    @Test
    void filtraPorTipo() throws Exception {
        mvc.perform(get("/api/eventos").param("tipo", "torneo"))
                .andExpect(jsonPath("$[*].tipo", everyItem(is("Torneo"))));
    }

    @Test
    void filtraPorPaisYCiudad() throws Exception {
        mvc.perform(get("/api/eventos").param("pais", "Chile").param("ciudad", "Santiago"))
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[*].pais", everyItem(is("Chile"))))
                .andExpect(jsonPath("$[*].ciudad", everyItem(is("Santiago"))));
    }

    @Test
    void filtraPorModalidad() throws Exception {
        mvc.perform(get("/api/eventos").param("modalidad", "ONLINE"))
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[*].modalidad", everyItem(is("ONLINE"))));
    }

    @Test
    void filtraPorRangoDeFechas() throws Exception {
        mvc.perform(get("/api/eventos").param("desde", "2026-12-01").param("hasta", "2026-12-31"))
                .andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/eventos").param("desde", "2026-11-21").param("hasta", "2026-11-21"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void combinaFiltros() throws Exception {
        mvc.perform(get("/api/eventos").param("pais", "Chile").param("modalidad", "PRESENCIAL")
                        .param("tipo", "Torneo"))
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void rangoInvertidoDevuelve400() throws Exception {
        mvc.perform(get("/api/eventos").param("desde", "2027-01-01").param("hasta", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void parametrosInvalidosDevuelven400() throws Exception {
        mvc.perform(get("/api/eventos").param("modalidad", "HIBRIDO")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/eventos").param("desde", "ayer")).andExpect(status().isBadRequest());
    }

    @Test
    void comodinesDeLikeNoSeInterpretan() throws Exception {
        mvc.perform(get("/api/eventos").param("nombre", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/eventos").param("nombre", "_"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void inyeccionSqlNoAfectaLaConsulta() throws Exception {
        mvc.perform(get("/api/eventos").param("nombre", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/eventos").param("ciudad", "Santiago'; DROP TABLE eventos; --"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/eventos")).andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void homeOrdenaPopularesPorInteresados() throws Exception {
        mvc.perform(get("/api/home"))
                .andExpect(jsonPath("$.populares[0].nombre").value("GamerFest Lima"))
                .andExpect(jsonPath("$.recientes[0].nombre").value("Fortnite Creative Jam"));
    }
}
