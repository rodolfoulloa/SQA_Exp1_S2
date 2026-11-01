package com.duoc.pixelmeet.api.repository;

import com.duoc.pixelmeet.api.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByActivoTrueOrderByOrdenAsc();
}
