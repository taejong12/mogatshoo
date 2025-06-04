package com.mogatshoo.dev.point.shop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.shop.entity.PointShopImgEntity;
import com.mogatshoo.dev.point.shop.repository.PointShopImgRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointShopImgServiceImpl implements PointShopImgService {

	private static final Logger logger = LoggerFactory.getLogger(PointShopImgServiceImpl.class);

	@Autowired
	private PointShopImgRepository pointShopImgRepository;

	@Override
	public PointShopImgEntity findByPointItemId(Long pointItemId) {
		try {
			return pointShopImgRepository.findByPointItemId(pointItemId);
		} catch (Exception e) {
			logger.error("포인트 아이템 ID로 이미지 조회 중 오류 발생 - pointItemId: {}", pointItemId, e);
			return null;
		}
	}
}
