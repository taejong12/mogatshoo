package com.mogatshoo.dev.point.shop.service;

import java.util.List;

import com.mogatshoo.dev.point.shop.entity.PointShopCategoryEntity;

public interface PointShopCategoryService {

	List<PointShopCategoryEntity> findAll();

	PointShopCategoryEntity findById(Integer pointCategoryId);

}
