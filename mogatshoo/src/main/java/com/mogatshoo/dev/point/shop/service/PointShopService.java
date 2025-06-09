package com.mogatshoo.dev.point.shop.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.point.shop.entity.PointShopEntity;

public interface PointShopService {

	Page<PointShopEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable);

	Page<PointShopEntity> findAll(Pageable pageable);

	PointShopEntity findById(Long pointItemId);

	Map<String, Object> checkBuyPossiblePointItem(Long pointItemId);

	void buyPointItem(Long pointItemId);

}
