package com.mogatshoo.dev.admin.point.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;

public interface AdminPointItemRepository extends JpaRepository<AdminPointItemEntity, Long> {

	Page<AdminPointItemEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

}
