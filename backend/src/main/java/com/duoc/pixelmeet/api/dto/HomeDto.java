package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.dto.ComunidadDtos.CompartidoDto;

import java.util.List;

public record HomeDto(List<EventoResumenDto> recientes, List<EventoResumenDto> populares, List<BannerDto> banners,
                      List<CompartidoDto> compartidos) {

    public HomeDto conCompartidos(List<CompartidoDto> lista) {
        return new HomeDto(recientes, populares, banners, lista);
    }
}
