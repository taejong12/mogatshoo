package com.mogatshoo.dev.point.shop.service;

import com.mogatshoo.dev.point.shop.entity.PointShopImgEntity;

public interface PointShopImgService {

	PointShopImgEntity findByPointItemId(Long pointItemId);

}
