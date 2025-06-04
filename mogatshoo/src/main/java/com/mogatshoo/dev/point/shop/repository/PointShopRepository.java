package com.mogatshoo.dev.point.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mogatshoo.dev.point.shop.entity.PointShopEntity;

import jakarta.persistence.LockModeType;

public interface PointShopRepository extends JpaRepository<PointShopEntity, Long> {

	Page<PointShopEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM PointShopEntity p WHERE p.pointItemId = :pointItemId")
	PointShopEntity findByIdForUpdate(@Param("pointItemId") Long pointItemId);

}
