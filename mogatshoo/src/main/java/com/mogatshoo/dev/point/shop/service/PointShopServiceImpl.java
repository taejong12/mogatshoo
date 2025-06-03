package com.mogatshoo.dev.point.shop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.shop.entity.PointShopEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopImgEntity;
import com.mogatshoo.dev.point.shop.repository.PointShopRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointShopServiceImpl implements PointShopService {

	private static final Logger logger = LoggerFactory.getLogger(PointShopServiceImpl.class);

	@Autowired
	private PointShopRepository pointShopRepository;

	@Autowired
	private PointShopImgService pointShopImgService;

	@Override
	public Page<PointShopEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable) {
		try {
			Page<PointShopEntity> page = pointShopRepository.findByPointCategoryId(pointCategoryId, pageable);

			page.forEach(shop -> {
				PointShopImgEntity imgFile = pointShopImgService.findByPointItemId(shop.getPointItemId());
				shop.setImgFile(imgFile);
			});

			return page;
		} catch (Exception e) {
			logger.error("포인트 카테고리 ID로 포인트샵 항목 조회 중 오류 발생. categoryId = {}", pointCategoryId, e);
			return Page.empty();
		}
	}

	@Override
	public Page<PointShopEntity> findAll(Pageable pageable) {
		try {
			Page<PointShopEntity> page = pointShopRepository.findAll(pageable);

			page.forEach(shop -> {
				PointShopImgEntity imgFile = pointShopImgService.findByPointItemId(shop.getPointItemId());
				shop.setImgFile(imgFile);
			});

			return page;
		} catch (Exception e) {
			logger.error("포인트샵 전체 목록 조회 중 오류 발생", e);
			return Page.empty();
		}
	}
}
