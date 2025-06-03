package com.mogatshoo.dev.point.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.point.shop.entity.PointShopEntity;

public interface PointShopRepository extends JpaRepository<PointShopEntity, Long> {

	Page<PointShopEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

}
