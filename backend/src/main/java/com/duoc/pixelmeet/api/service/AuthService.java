package com.duoc.pixelmeet.api.service;

import com.duoc.pixelmeet.api.dto.LoginRequest;
import com.duoc.pixelmeet.api.dto.LoginResponse;
import com.duoc.pixelmeet.api.dto.RegistroRequest;
import com.duoc.pixelmeet.api.dto.UsuarioDto;
import com.duoc.pixelmeet.api.exception.ConflictoException;
import com.duoc.pixelmeet.api.exception.DemasiadosIntentosException;
import com.duoc.pixelmeet.api.model.Rol;
import com.duoc.pixelmeet.api.model.Usuario;
import com.duoc.pixelmeet.api.repository.RolRepository;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import com.duoc.pixelmeet.api.security.JwtService;
import com.duoc.pixelmeet.api.security.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService intentos;
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       LoginAttemptService intentos, UsuarioRepository usuarios, RolRepository roles,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.intentos = intentos;
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    /** Valida las credenciales contra la BD (BCrypt) y emite un JWT. */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String login = req.username().trim();
        if (intentos.estaBloqueado(login)) {
            log.warn("Login bloqueado temporalmente para '{}'", login);
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intenta nuevamente en unos minutos");
        }
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, req.password()));
        } catch (AuthenticationException ex) {
            intentos.falla(login);
            log.warn("Login fallido para '{}'", login);
            throw ex;
        }
        intentos.exito(login);
        UserDetails user = (UserDetails) auth.getPrincipal();
        JwtService.TokenEmitido token = jwtService.generar(user);
        Usuario usuario = usuarios.findByUsernameIgnoreCase(user.getUsername()).orElseThrow();
        UsuarioDto dto = UsuarioDto.desde(usuario);
        log.info("Login exitoso para '{}'", user.getUsername());
        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), dto.username(), dto.nombre(),
                dto.roles());
    }

    /** Crea una cuenta con rol USER. El correo es el nombre de inicio de sesion. */
    @Transactional
    public UsuarioDto registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByUsernameIgnoreCaseOrEmailIgnoreCase(correo, correo)) {
            throw new ConflictoException("correo", "El correo ya está registrado");
        }
        Usuario u = new Usuario(correo, correo, passwordEncoder.encode(req.password()), req.nombre().trim(),
                aliasDisponible(correo));
        u.agregarRol(roles.findByNombre(Rol.USER).orElseThrow());
        return UsuarioDto.desde(usuarios.save(u));
    }

    @Transactional(readOnly = true)
    public UsuarioDto actual(String username) {
        return usuarios.findByUsernameIgnoreCase(username).map(UsuarioDto::desde).orElseThrow();
    }

    private String aliasDisponible(String correo) {
        String base = correo.substring(0, correo.indexOf('@')).replaceAll("[^A-Za-z0-9_]", "_");
        if (base.length() < 3) {
            base = (base + "___").substring(0, 3);
        }
        base = base.length() > 16 ? base.substring(0, 16) : base;
        String alias = base;
        int n = 1;
        while (usuarios.existsByNombreUsuarioIgnoreCase(alias)) {
            alias = base + "_" + n++;
        }
        return alias;
    }
}
