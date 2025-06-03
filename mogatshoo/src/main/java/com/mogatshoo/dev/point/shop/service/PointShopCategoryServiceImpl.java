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
			return pointShopCategoryRepository.findAll(sort);
		} catch (Exception e) {
			logger.error("포인트샵 카테고리 조회 중 오류 발생", e);
			return Collections.emptyList(); // 예외 발생 시 빈 리스트 반환
		}
	}

}
