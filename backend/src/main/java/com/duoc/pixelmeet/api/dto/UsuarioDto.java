package com.duoc.pixelmeet.api.dto;

import com.duoc.pixelmeet.api.model.Rol;
import com.duoc.pixelmeet.api.model.Usuario;

import java.util.List;

/** Datos publicos de una cuenta (sin contrasena). */
public record UsuarioDto(Long id, String username, String email, String nombre, boolean activo, List<String> roles) {

    public static UsuarioDto desde(Usuario u) {
        return new UsuarioDto(u.getId(), u.getUsername(), u.getEmail(), u.getNombre(), u.isActivo(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList());
    }
}
