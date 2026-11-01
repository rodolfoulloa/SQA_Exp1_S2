package com.duoc.pixelmeet.web;

import com.duoc.pixelmeet.web.client.ApiDtos.LoginResponse;
import com.duoc.pixelmeet.web.client.ApiDtos.Usuario;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.BusquedaForm;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PixelMeetApiClient api;

    @BeforeEach
    void apiSimulada() {
        when(api.home()).thenReturn(Datos.home());
        when(api.filtros()).thenReturn(Datos.filtros());
        when(api.buscar(any())).thenReturn(List.of(Datos.resumen(1, "Copa Andina de Valorant")));
        when(api.detalle(anyLong(), anyString())).thenReturn(Datos.detalle(1, "Copa Andina de Valorant"));
        when(api.perfil(anyString())).thenReturn(Datos.perfil("gamer1"));
    }

    // ---------- Rutas publicas ----------

    @ParameterizedTest
    @ValueSource(strings = {"/", "/home", "/eventos", "/login", "/registro", "/css/app.css",
            "/webjars/bootstrap/css/bootstrap.min.css", "/images/eventos/valorant-1.svg",
            "/images/banners/banner-neokeys.svg"})
    void rutasPublicasRespondenSinLogin(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }

    @Test
    void homeMuestraRecientesPopularesBannersYAccesoAlLogin() throws Exception {
        mvc.perform(get("/home"))
                .andExpect(content().string(containsString("Eventos más recientes")))
                .andExpect(content().string(containsString("Eventos más populares")))
                .andExpect(content().string(containsString("GamerFest Lima")))
                .andExpect(content().string(containsString("NeoKeys")))
                .andExpect(content().string(containsString("Iniciar sesión")));
    }

    @Test
    void busquedaPublicaEnviaLosFiltrosALaApi() throws Exception {
        mvc.perform(get("/eventos").param("nombre", "copa").param("videojuego", "Valorant")
                        .param("tipo", "Torneo").param("pais", "Chile").param("ciudad", "Santiago")
                        .param("desde", "2026-11-01").param("hasta", "2026-12-31").param("modalidad", "ONLINE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 evento(s) encontrado(s)")))
                .andExpect(content().string(containsString("Inicia sesión para ver el detalle")));

        ArgumentCaptor<BusquedaForm> captor = ArgumentCaptor.forClass(BusquedaForm.class);
        verify(api).buscar(captor.capture());
        BusquedaForm f = captor.getValue();
        assertThat(f.getNombre()).isEqualTo("copa");
        assertThat(f.getVideojuego()).isEqualTo("Valorant");
        assertThat(f.getPais()).isEqualTo("Chile");
        assertThat(f.getModalidad()).isEqualTo("ONLINE");
        assertThat(f.getDesde()).hasToString("2026-11-01");
    }

    @Test
    void modalidadDesconocidaSeIgnora() throws Exception {
        mvc.perform(get("/eventos").param("modalidad", "<script>")).andExpect(status().isOk());
        ArgumentCaptor<BusquedaForm> captor = ArgumentCaptor.forClass(BusquedaForm.class);
        verify(api).buscar(captor.capture());
        assertThat(captor.getValue().getModalidad()).isNull();
    }

    @Test
    void errorDeValidacionDeLaApiSeMuestraEnLaBusqueda() throws Exception {
        when(api.buscar(any())).thenThrow(new ApiException(ApiException.Tipo.VALIDACION,
                "La fecha 'hasta' no puede ser anterior a 'desde'", Map.of()));
        mvc.perform(get("/eventos").param("desde", "2027-01-01").param("hasta", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser anterior")));
    }

    @Test
    void contenidoDeLaApiSeEscapaParaEvitarXss() throws Exception {
        when(api.buscar(any())).thenReturn(List.of(Datos.resumen(9, "<script>alert('xss')</script>")));
        mvc.perform(get("/eventos"))
                .andExpect(content().string(not(containsString("<script>alert"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    // ---------- Rutas privadas ----------

    @ParameterizedTest
    @ValueSource(strings = {"/eventos/1", "/perfil", "/admin/usuarios"})
    void rutasPrivadasRedirigenALogin(String ruta) throws Exception {
        mvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(api, never()).detalle(anyLong(), anyString());
    }

    @Test
    void detalleAutenticadoUsaElJwtDeLaSesion() throws Exception {
        mvc.perform(get("/eventos/1").with(authentication(Datos.gamer())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Descripción completa del evento")))
                .andExpect(content().string(containsString("Requisitos de participación")))
                .andExpect(content().string(containsString("Andes Esports")))
                .andExpect(content().string(containsString("valorant-2.svg")))
                .andExpect(content().string(not(containsString(Datos.TOKEN_GAMER))));
        verify(api).detalle(1L, Datos.TOKEN_GAMER);
    }

    @Test
    void eventoInexistenteDevuelve404() throws Exception {
        when(api.detalle(eq(999L), anyString()))
                .thenThrow(new ApiException(ApiException.Tipo.NO_ENCONTRADO, "Evento no encontrado", Map.of()));
        mvc.perform(get("/eventos/999").with(authentication(Datos.gamer())))
                .andExpect(status().isNotFound());
    }

    @Test
    void tokenVencidoCierraLaSesionYPideLoginNuevamente() throws Exception {
        when(api.detalle(anyLong(), anyString()))
                .thenThrow(new ApiException(ApiException.Tipo.NO_AUTENTICADO, "Token vencido", Map.of()));
        mvc.perform(get("/eventos/1").with(authentication(Datos.gamer())))
                .andExpect(redirectedUrl("/login?expirado"));
    }

    @Test
    void apiCaidaMuestraPagina503SinDetalles() throws Exception {
        when(api.detalle(anyLong(), anyString()))
                .thenThrow(new ApiException(ApiException.Tipo.NO_DISPONIBLE, "Connection refused: backend:8080",
                        new RuntimeException()));
        mvc.perform(get("/eventos/1").with(authentication(Datos.gamer())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(not(containsString("backend:8080"))));
    }

    // ---------- Autorizacion por roles ----------

    @Test
    void usuarioSinRolAdminRecibe403() throws Exception {
        mvc.perform(get("/admin/usuarios").with(authentication(Datos.gamer())))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeElListadoDeUsuarios() throws Exception {
        when(api.usuarios(Datos.TOKEN_ADMIN)).thenReturn(List.of(
                new Usuario(1L, "admin", "admin@pixelmeet.cl", "Administrador", true, List.of("ADMIN", "USER"))));
        mvc.perform(get("/admin/usuarios").with(authentication(Datos.admin())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin@pixelmeet.cl")));
    }

    @Test
    void menuDeAdministracionSoloParaAdmin() throws Exception {
        mvc.perform(get("/home").with(authentication(Datos.gamer())))
                .andExpect(content().string(not(containsString("Administración"))));
        mvc.perform(get("/home").with(authentication(Datos.admin())))
                .andExpect(content().string(containsString("Administración")));
    }

    // ---------- Login contra la API ----------

    @Test
    void loginCorrectoAutenticaConLosRolesQueEntregaLaApi() throws Exception {
        when(api.login("admin", "Admin123!")).thenReturn(new LoginResponse("jwt.de.prueba", "Bearer",
                Instant.now().plusSeconds(3600), "admin", "Administrador", List.of("ADMIN", "USER")));
        mvc.perform(formLogin().user("admin").password("Admin123!"))
                .andExpect(authenticated().withUsername("admin").withRoles("ADMIN", "USER"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void credencialesInvalidasNoAutentican() throws Exception {
        when(api.login(anyString(), anyString())).thenThrow(
                new ApiException(ApiException.Tipo.NO_AUTENTICADO, "Usuario o contraseña incorrectos", Map.of()));
        mvc.perform(formLogin().user("admin").password("mala"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void bloqueoPorIntentosSeInformaAlUsuario() throws Exception {
        when(api.login(anyString(), anyString())).thenThrow(
                new ApiException(ApiException.Tipo.DEMASIADOS_INTENTOS, "Demasiados intentos", Map.of()));
        mvc.perform(formLogin().user("admin").password("mala"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?bloqueado"));
    }

    @Test
    void apiCaidaDuranteElLogin() throws Exception {
        when(api.login(anyString(), anyString())).thenThrow(
                new ApiException(ApiException.Tipo.NO_DISPONIBLE, "caida", new RuntimeException()));
        mvc.perform(formLogin().user("admin").password("Admin123!"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?servicio"));
    }

    @Test
    void loginSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "Admin123!"))
                .andExpect(status().isForbidden());
        verify(api, never()).login(anyString(), anyString());
    }

    // ---------- Registro y perfil ----------

    @Test
    void registroValidoSeEnviaALaApi() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Nueva Jugadora").param("correo", "nueva@duoc.cl")
                        .param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
                .andExpect(redirectedUrl("/login?registrado"));
        verify(api).registrar(any());
    }

    @Test
    void registroDuplicadoMuestraElErrorDeLaApi() throws Exception {
        when(api.registrar(any())).thenThrow(new ApiException(ApiException.Tipo.VALIDACION,
                "El correo ya está registrado", Map.of("correo", "El correo ya está registrado")));
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Repetida").param("correo", "gamer1@pixelmeet.cl")
                        .param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El correo ya está registrado")));
    }

    @Test
    void registroInvalidoNoLlamaALaApi() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "").param("correo", "no-es-correo")
                        .param("password", "corta").param("confirmarPassword", "otra"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresa un correo válido")));
        verify(api, never()).registrar(any());
    }

    @Test
    void registroSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/registro").param("nombre", "X").param("correo", "x@duoc.cl")
                        .param("password", "Clave1234").param("confirmarPassword", "Clave1234"))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfilSeLeeYGuardaConElJwt() throws Exception {
        mvc.perform(get("/perfil").with(authentication(Datos.gamer())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("camirojas")));
        mvc.perform(post("/perfil").with(authentication(Datos.gamer())).with(csrf())
                        .param("nombreUsuario", "ProGamer_2").param("avatar", "avatar-3")
                        .param("juegos", "Valorant", "Tekken 8"))
                .andExpect(redirectedUrl("/perfil"));
        verify(api).actualizarPerfil(any(), eq(Datos.TOKEN_GAMER));
    }

    // ---------- Cabeceras ----------

    @Test
    void respuestasIncluyenCabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/home"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().exists("Permissions-Policy"));
    }
}
