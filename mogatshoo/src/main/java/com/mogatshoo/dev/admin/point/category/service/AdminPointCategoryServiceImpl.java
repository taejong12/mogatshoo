package com.mogatshoo.dev.admin.point.category.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;
import com.mogatshoo.dev.admin.point.category.repository.AdminPointCategoryRepository;
import com.mogatshoo.dev.common.authentication.CommonUserName;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointCategoryServiceImpl implements AdminPointCategoryService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointCategoryServiceImpl.class);

	@Autowired
	private AdminPointCategoryRepository adminPointCategoryRepository;

	@Autowired
	private CommonUserName commonUserName;

	@Override
	public List<AdminPointCategoryEntity> findAll() {
		Sort sort = Sort.by(Sort.Order.asc("pointCategorySortOrder"), Sort.Order.desc("pointCategoryUpdate"));
		return adminPointCategoryRepository.findAll(sort);
	}

	@Override
	public void save(AdminPointCategoryEntity adminPointCategoryEntity) {

		try {
			String memberId = commonUserName.getUserName();

			if (memberId == null || memberId.isBlank()) {
				throw new IllegalStateException("현재 로그인된 사용자가 없습니다.");
			}

			adminPointCategoryEntity.setMemberId(memberId);
			adminPointCategoryRepository.save(adminPointCategoryEntity);

		} catch (Exception e) {
			logger.error("카테고리 저장 중 오류 발생: " + e);
			throw new RuntimeException("카테고리 저장 실패", e);
		}
	}

	@Override
	public Page<AdminPointCategoryEntity> findAllPageable(Pageable pageable) {
		try {
			Page<AdminPointCategoryEntity> result = adminPointCategoryRepository.findAll(pageable);
			logger.info("포인트 카테고리 페이지 조회 성공 - 페이지 번호: {}", pageable.getPageNumber());
			return result;
		} catch (Exception e) {
			logger.error("포인트 카테고리 페이지 조회 실패", e);
			return Page.empty();
		}
	}

	@Override
	public AdminPointCategoryEntity findById(Integer pointCategoryId) {
		try {
			Optional<AdminPointCategoryEntity> optionalEntity = adminPointCategoryRepository.findById(pointCategoryId);
			if (optionalEntity.isPresent()) {
				logger.info("포인트 카테고리 조회 성공 - ID: {}", pointCategoryId);
				return optionalEntity.get();
			} else {
				logger.warn("포인트 카테고리가 존재하지 않음 - ID: {}", pointCategoryId);
				return null;
			}
		} catch (Exception e) {
			logger.error("포인트 카테고리 조회 중 오류 발생 - ID: {}", pointCategoryId, e);
			return null;
		}
	}

}
