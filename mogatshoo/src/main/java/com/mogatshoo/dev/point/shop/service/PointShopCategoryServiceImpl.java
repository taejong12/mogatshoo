package com.mogatshoo.dev.point.shop.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.shop.entity.PointShopCategoryEntity;
import com.mogatshoo.dev.point.shop.repository.PointShopCategoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointShopCategoryServiceImpl implements PointShopCategoryService {

	private static final Logger logger = LoggerFactory.getLogger(PointShopCategoryServiceImpl.class);

	@Autowired
	private PointShopCategoryRepository pointShopCategoryRepository;

	@Override
	public List<PointShopCategoryEntity> findAll() {
		try {
			Sort sort = Sort.by(Sort.Order.asc("pointCategorySortOrder"), Sort.Order.desc("pointCategoryUpdate"));
			List<PointShopCategoryEntity> categories = pointShopCategoryRepository.findAll(sort);
			logger.info("포인트샵 카테고리 조회 성공 - 조회된 카테고리 수: {}", categories.size());
			return categories;
		} catch (Exception e) {
			logger.error("포인트샵 카테고리 조회 중 오류 발생: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public PointShopCategoryEntity findById(Integer pointCategoryId) {
		try {
			PointShopCategoryEntity category = pointShopCategoryRepository.findById(pointCategoryId).orElse(null);

			if (category == null) {
				logger.warn("카테고리를 찾을 수 없습니다. ID: {}", pointCategoryId);
			} else {
				logger.info("카테고리 조회 성공. ID: {}", pointCategoryId);
			}

			return category;

		} catch (Exception e) {
			logger.error("카테고리 조회 중 오류 발생. ID: {}", pointCategoryId, e);
			throw e;
		}
	}

}
