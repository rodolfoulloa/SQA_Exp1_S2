package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.EventoResumen;
import com.duoc.pixelmeet.web.client.ApiDtos.Filtros;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.BusquedaForm;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.form.EventoForm;
import com.duoc.pixelmeet.web.security.SesionUsuario;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private static final String VISTA_NUEVO = "evento-nuevo";
    private static final Set<String> CAMPOS_FORM = Set.of("nombre", "videojuego", "tipo", "modalidad", "pais",
            "ciudad", "fechaHora", "resumen", "descripcion", "ubicacion", "enlaceAcceso", "requisitos",
            "inscripcion", "precio", "cupos");

    private final PixelMeetApiClient api;

    public EventoController(PixelMeetApiClient api) {
        this.api = api;
    }

    /** [Publica] Busqueda y filtrado; muestra informacion resumida. */
    @GetMapping
    public String buscar(@ModelAttribute("busqueda") BusquedaForm busqueda, Model model) {
        Filtros opciones;
        List<EventoResumen> eventos;
        try {
            opciones = api.filtros();
            eventos = api.buscar(busqueda);
        } catch (ApiException ex) {
            if (ex.getTipo() != ApiException.Tipo.VALIDACION) {
                throw ex;
            }
            opciones = api.filtros();
            eventos = List.of();
            model.addAttribute("errorBusqueda", ex.getMessage());
        }
        model.addAttribute("opciones", opciones);
        model.addAttribute("eventos", eventos);
        return "eventos";
    }

    /** [Privada] Formulario para publicar un evento. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("eventoForm", new EventoForm());
        return VISTA_NUEVO;
    }

    /** [Privada] Publica el evento en la API con el JWT de la sesion; el organizador lo asigna la API. */
    @PostMapping("/nuevo")
    public String publicar(@Valid @ModelAttribute("eventoForm") EventoForm form, BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return VISTA_NUEVO;
        }
        try {
            long id = api.publicarEvento(form.aRequest(), SesionUsuario.token()).id();
            redirect.addFlashAttribute("mensaje", "Evento publicado correctamente");
            return "redirect:/eventos/" + id;
        } catch (ApiException ex) {
            if (ex.getTipo() != ApiException.Tipo.VALIDACION) {
                throw ex;
            }
            ex.getCampos().forEach((campo, msg) -> {
                if (CAMPOS_FORM.contains(campo)) {
                    result.rejectValue(campo, "api", msg);
                }
            });
            if (!result.hasErrors()) {
                result.reject("api", ex.getMessage());
            }
            return VISTA_NUEVO;
        }
    }

    /** [Privada] Detalle completo; la API valida el JWT del usuario. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model) {
        String token = SesionUsuario.token();
        model.addAttribute("evento", api.detalle(id, token));
        model.addAttribute("comunidad", api.comunidad(id, token));
        model.addAttribute("redes", RedesSociales.CANALES);
        return "evento-detalle";
    }
}
