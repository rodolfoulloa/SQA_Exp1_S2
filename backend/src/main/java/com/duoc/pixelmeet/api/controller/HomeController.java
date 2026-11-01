package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.HomeDto;
import com.duoc.pixelmeet.api.service.EventoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final EventoService eventoService;

    public HomeController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    /** [Publica] Eventos recientes, populares y banners comerciales. */
    @GetMapping
    public HomeDto home() {
        return eventoService.home(6);
    }
}
