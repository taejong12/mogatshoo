package com.mogatshoo.dev.admin.point.category.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;

public interface AdminPointCategoryService {

	List<AdminPointCategoryEntity> findAll();

	void save(AdminPointCategoryEntity adminPointCategoryEntity);

	Page<AdminPointCategoryEntity> findAllPageable(Pageable pageable);

	AdminPointCategoryEntity findById(Integer pointCategoryId);

	void deletePointCategory(Integer pointCategoryId);

	void updatePointCategory(AdminPointCategoryEntity adminPointCategoryEntity, List<AdminPointItemEntity> pointItemList);

}
