package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Evento;
import com.duoc.pixelmeet.api.model.Modalidad;

import java.time.LocalDateTime;

/** Informacion resumida (publica) de un evento, usada en el home y en la busqueda. */
public record EventoResumenDto(Long id, String nombre, String videojuego, String tipo, Modalidad modalidad,
                               String pais, String ciudad, LocalDateTime fechaHora, String resumen,
                               String precio, int interesados, boolean destacado, String portada) {

    public static EventoResumenDto desde(Evento e) {
        String portada = e.getFotos().isEmpty() ? null : e.getFotos().getFirst();
        return new EventoResumenDto(e.getId(), e.getNombre(), e.getVideojuego(), e.getTipo(), e.getModalidad(),
                e.getPais(), e.getCiudad(), e.getFechaHora(), e.getResumen(), e.getPrecio(),
                e.getInteresados(), e.isDestacado(), portada);
    }
}
