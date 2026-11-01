package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.PerfilDto;
import com.duoc.pixelmeet.api.dto.PerfilRequest;
import com.duoc.pixelmeet.api.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** [Privada] Perfil del usuario autenticado. El usuario se toma del token, nunca de la peticion. */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public PerfilDto ver(Authentication auth) {
        return perfilService.obtener(auth.getName());
    }

    @PutMapping
    public PerfilDto actualizar(Authentication auth, @Valid @RequestBody PerfilRequest req) {
        return perfilService.actualizar(auth.getName(), req);
    }
}
