package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.EventoResumen;
import com.duoc.pixelmeet.web.client.ApiDtos.Filtros;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.BusquedaForm;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import com.duoc.pixelmeet.web.security.SesionUsuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/eventos")
public class EventoController {

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

    /** [Privada] Detalle completo; la API valida el JWT del usuario. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable long id, Model model) {
        model.addAttribute("evento", api.detalle(id, SesionUsuario.token()));
        return "evento-detalle";
    }
}
