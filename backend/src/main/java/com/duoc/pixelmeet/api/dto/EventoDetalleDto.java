package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Evento;
import com.duoc.pixelmeet.api.model.Modalidad;

import java.time.LocalDateTime;
import java.util.List;

/** Detalle completo de un evento (solo para usuarios autenticados). */
public record EventoDetalleDto(Long id, String nombre, String videojuego, String tipo, Modalidad modalidad,
                               String pais, String ciudad, LocalDateTime fechaHora, String resumen,
                               String descripcion, String ubicacion, String enlaceAcceso, String organizador,
                               String requisitos, String inscripcion, String precio, Integer cupos,
                               int interesados, boolean destacado, List<String> fotos) {

    public static EventoDetalleDto desde(Evento e) {
        return new EventoDetalleDto(e.getId(), e.getNombre(), e.getVideojuego(), e.getTipo(), e.getModalidad(),
                e.getPais(), e.getCiudad(), e.getFechaHora(), e.getResumen(), e.getDescripcion(),
                e.getUbicacion(), e.getEnlaceAcceso(), e.getOrganizador(), e.getRequisitos(),
                e.getInscripcion(), e.getPrecio(), e.getCupos(), e.getInteresados(), e.isDestacado(),
                List.copyOf(e.getFotos()));
    }
}
