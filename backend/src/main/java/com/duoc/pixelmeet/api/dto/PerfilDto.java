package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Usuario;

import java.util.List;

public record PerfilDto(String cuenta, String nombre, String nombreUsuario, String avatar, List<String> juegos,
                        boolean notifCorreo, boolean notifPush, boolean notifNuevosEventos) {

    public static PerfilDto desde(Usuario u) {
        return new PerfilDto(u.getUsername(), u.getNombre(), u.getNombreUsuario(), u.getAvatar(),
                List.copyOf(u.getJuegos()), u.isNotifCorreo(), u.isNotifPush(), u.isNotifNuevosEventos());
    }
}
