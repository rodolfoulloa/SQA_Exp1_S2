package com.duoc.pixelmeet.api.security;

import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carga los usuarios y sus roles desde la base de datos. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        String valor = login == null ? "" : login.trim();
        return usuarios.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(valor, valor)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .disabled(!u.isActivo())
                        .authorities(u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre()))
                                .toList())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
