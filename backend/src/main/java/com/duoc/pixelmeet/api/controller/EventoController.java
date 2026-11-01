package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.BusquedaEventos;
import com.duoc.pixelmeet.api.dto.EventoDetalleDto;
import com.duoc.pixelmeet.api.dto.EventoResumenDto;
import com.duoc.pixelmeet.api.dto.FiltrosDto;
import com.duoc.pixelmeet.api.model.Modalidad;
import com.duoc.pixelmeet.api.service.EventoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    /** [Publica] Busqueda con filtros opcionales; devuelve informacion resumida. */
    @GetMapping
    public List<EventoResumenDto> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String videojuego,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Modalidad modalidad) {
        return eventoService.buscar(
                new BusquedaEventos(nombre, videojuego, tipo, pais, ciudad, desde, hasta, modalidad));
    }

    /** [Publica] Valores disponibles para los filtros. */
    @GetMapping("/filtros")
    public FiltrosDto filtros() {
        return eventoService.filtros();
    }

    /** [Privada] Detalle completo del evento (requiere JWT). */
    @GetMapping("/{id}")
    public EventoDetalleDto detalle(@PathVariable Long id) {
        return eventoService.detalle(id);
    }
}
