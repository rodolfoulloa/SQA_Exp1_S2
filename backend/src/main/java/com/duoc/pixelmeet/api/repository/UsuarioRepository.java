package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    /** Permite iniciar sesion con el nombre de usuario o con el correo. */
    Optional<Usuario> findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByNombreUsuarioIgnoreCaseAndIdNot(String nombreUsuario, Long id);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
}
