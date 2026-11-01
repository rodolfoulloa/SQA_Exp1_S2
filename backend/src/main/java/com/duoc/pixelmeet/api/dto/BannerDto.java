package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Banner;

public record BannerDto(Long id, String marca, String mensaje, String llamado, String imagen) {

    public static BannerDto desde(Banner b) {
        return new BannerDto(b.getId(), b.getMarca(), b.getMensaje(), b.getLlamado(), b.getImagen());
    }
}
