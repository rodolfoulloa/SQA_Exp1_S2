package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.HomeDto;
import com.duoc.pixelmeet.api.service.ComunidadService;
import com.duoc.pixelmeet.api.service.EventoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final EventoService eventoService;
    private final ComunidadService comunidadService;

    public HomeController(EventoService eventoService, ComunidadService comunidadService) {
        this.eventoService = eventoService;
        this.comunidadService = comunidadService;
    }

    /** [Publica] Eventos recientes, populares, banners comerciales y lo compartido por la comunidad. */
    @GetMapping
    public HomeDto home() {
        return eventoService.home(6).conCompartidos(comunidadService.compartidosRecientes());
    }
}
