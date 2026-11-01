package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.dto.PerfilDto;
import com.duoc.pixelmeet.api.dto.PerfilRequest;
import com.duoc.pixelmeet.api.exception.ConflictoException;
import com.duoc.pixelmeet.api.exception.RecursoNoEncontradoException;
import com.duoc.pixelmeet.api.model.Usuario;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerfilService {

    private final UsuarioRepository usuarios;

    public PerfilService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public PerfilDto obtener(String username) {
        return PerfilDto.desde(buscar(username));
    }

    /** Actualiza el perfil del usuario autenticado; avatar y juegos se filtran por lista blanca. */
    @Transactional
    public PerfilDto actualizar(String username, PerfilRequest req) {
        Usuario u = buscar(username);
        String alias = req.nombreUsuario().trim();
        if (usuarios.existsByNombreUsuarioIgnoreCaseAndIdNot(alias, u.getId())) {
            throw new ConflictoException("nombreUsuario", "Ese nombre de usuario ya está en uso");
        }
        u.setNombreUsuario(alias);
        u.setAvatar(Catalogos.AVATARES.contains(req.avatar()) ? req.avatar() : Catalogos.AVATARES.getFirst());
        List<String> juegos = req.juegos() == null ? List.of()
                : req.juegos().stream().filter(Catalogos.JUEGOS::contains).distinct().toList();
        u.setJuegos(juegos);
        u.setNotifCorreo(Boolean.TRUE.equals(req.notifCorreo()));
        u.setNotifPush(Boolean.TRUE.equals(req.notifPush()));
        u.setNotifNuevosEventos(Boolean.TRUE.equals(req.notifNuevosEventos()));
        return PerfilDto.desde(u);
    }

    private Usuario buscar(String username) {
        return usuarios.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
