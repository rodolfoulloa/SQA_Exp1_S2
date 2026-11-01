package com.duoc.pixelmeet.api.controller;

import com.duoc.pixelmeet.api.dto.LoginRequest;
import com.duoc.pixelmeet.api.dto.LoginResponse;
import com.duoc.pixelmeet.api.dto.RegistroRequest;
import com.duoc.pixelmeet.api.dto.UsuarioDto;
import com.duoc.pixelmeet.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** [Publica] Recibe usuario (o correo) y contrasena; responde con el token JWT. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /** [Publica] Crea una cuenta nueva con rol USER. */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registrar(@Valid @RequestBody RegistroRequest req) {
        return authService.registrar(req);
    }

    /** [Privada] Datos del usuario dueno del token. */
    @GetMapping("/me")
    public UsuarioDto me(Authentication auth) {
        return authService.actual(auth.getName());
    }
}
