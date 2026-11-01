package com.duoc.pixelmeet.api.dto;

import java.util.List;

/** Valores disponibles para los filtros de busqueda. */
public record FiltrosDto(List<String> videojuegos, List<String> tipos, List<String> paises, List<String> ciudades,
                         List<String> modalidades) {
}
