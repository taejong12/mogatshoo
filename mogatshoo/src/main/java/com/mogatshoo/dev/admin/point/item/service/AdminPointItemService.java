package com.mogatshoo.dev.admin.point.item.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;

public interface AdminPointItemService {

	Page<AdminPointItemEntity> findAll(Pageable pageable);

	Page<AdminPointItemEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

}
