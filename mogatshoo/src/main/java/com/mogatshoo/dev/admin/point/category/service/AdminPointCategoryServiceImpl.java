package com.mogatshoo.dev.admin.point.category.service;

import java.util.List;

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
		return adminPointCategoryRepository.findAll(pageable);
	}
}
