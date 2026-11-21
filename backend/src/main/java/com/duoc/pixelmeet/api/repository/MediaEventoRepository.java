package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.MediaEvento;
import com.duoc.pixelmeet.api.model.TipoMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaEventoRepository extends JpaRepository<MediaEvento, Long> {

    List<MediaEvento> findByEventoIdOrderByCreadoEnAsc(Long eventoId);

    long countByEventoIdAndTipo(Long eventoId, TipoMedia tipo);

    /** Busca el contenido dentro del evento indicado: evita acceder a media de otro evento por su id. */
    Optional<MediaEvento> findByIdAndEventoId(Long id, Long eventoId);
}
