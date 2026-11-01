package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.Evento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    /** Eventos futuros publicados mas recientemente. */
    List<Evento> findByFechaHoraAfterOrderByPublicadoEnDesc(LocalDateTime desde, Pageable pageable);

    /** Eventos futuros con mas interesados. */
    List<Evento> findByFechaHoraAfterOrderByInteresadosDesc(LocalDateTime desde, Pageable pageable);

    @Query("select distinct e.videojuego from Evento e order by e.videojuego")
    List<String> videojuegos();

    @Query("select distinct e.tipo from Evento e order by e.tipo")
    List<String> tipos();

    @Query("select distinct e.pais from Evento e order by e.pais")
    List<String> paises();

    @Query("select distinct e.ciudad from Evento e order by e.ciudad")
    List<String> ciudades();
}
