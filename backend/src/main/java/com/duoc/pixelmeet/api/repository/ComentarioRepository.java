package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findTop50ByEventoIdOrderByCreadoEnDesc(Long eventoId);

    Optional<Comentario> findByIdAndEventoId(Long id, Long eventoId);
}
