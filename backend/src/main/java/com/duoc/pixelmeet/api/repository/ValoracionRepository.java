package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    Optional<Valoracion> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    long countByEventoId(Long eventoId);

    @Query("select coalesce(avg(v.estrellas), 0) from Valoracion v where v.evento.id = :eventoId")
    double promedio(@Param("eventoId") Long eventoId);
}
