package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.CanalCompartir;
import com.duoc.pixelmeet.api.model.Compartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    long countByEventoId(Long eventoId);

    List<Compartido> findTop6ByCanalOrderByCreadoEnDesc(CanalCompartir canal);
}
