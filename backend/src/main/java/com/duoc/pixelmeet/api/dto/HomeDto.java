package com.duoc.pixelmeet.api.dto;

import java.util.List;

public record HomeDto(List<EventoResumenDto> recientes, List<EventoResumenDto> populares, List<BannerDto> banners) {
}
