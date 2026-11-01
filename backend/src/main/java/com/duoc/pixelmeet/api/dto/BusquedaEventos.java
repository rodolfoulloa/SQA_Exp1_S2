package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Modalidad;

import java.time.LocalDate;

/** Criterios de busqueda de eventos (todos opcionales). */
public record BusquedaEventos(String nombre, String videojuego, String tipo, String pais, String ciudad,
                              LocalDate desde, LocalDate hasta, Modalidad modalidad) {
}
