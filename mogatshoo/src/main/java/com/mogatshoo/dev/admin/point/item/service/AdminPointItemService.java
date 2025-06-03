package com.mogatshoo.dev.admin.point.item.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;

public interface AdminPointItemService {

	Page<AdminPointItemEntity> findAll(Pageable pageable);

	Page<AdminPointItemEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

	void save(AdminPointItemEntity adminPointItemEntity);

	AdminPointItemEntity findById(Long pointItemId);

	void deletePointItem(Long pointItemId);

	void updatePointItem(AdminPointItemEntity adminPointItemEntity);

	Map<String, Object> deletePointCategoryCheck(Integer pointCategoryId);

}
