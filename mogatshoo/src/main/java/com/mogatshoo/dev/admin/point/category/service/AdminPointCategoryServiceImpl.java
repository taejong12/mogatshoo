package com.mogatshoo.dev.admin.point.category.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemImgService;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemService;
import com.mogatshoo.dev.common.authentication.CommonUserName;
import com.mogatshoo.dev.config.file.FirebaseStorageService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointCategoryServiceImpl implements AdminPointCategoryService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointCategoryServiceImpl.class);

	@Autowired
	private AdminPointCategoryRepository adminPointCategoryRepository;

	@Autowired
	private AdminPointItemImgService adminPointItemImgService;

	@Autowired
	private CommonUserName commonUserName;

	@Autowired
	private FirebaseStorageService firebaseStorageService;

	@Override
	public List<AdminPointCategoryEntity> findAll() {
		try {
			Sort sort = Sort.by(Sort.Order.asc("pointCategorySortOrder"), Sort.Order.desc("pointCategoryUpdate"));
			List<AdminPointCategoryEntity> categoryList = adminPointCategoryRepository.findAll(sort);
			logger.info("카테고리 전체 조회 성공: {}건", categoryList.size());
			return categoryList;
		} catch (Exception e) {
			logger.error("카테고리 전체 조회 중 오류 발생", e);
			// 또는 return null;
			return Collections.emptyList(); 
		}
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

	@Override
	public void deletePointCategory(Integer pointCategoryId) {
		try {
			AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryRepository.findById(pointCategoryId)
					.orElse(null);

			if (adminPointCategoryEntity == null) {
				logger.warn("deletePointCategory: 존재하지 않는 카테고리입니다. ID: {}", pointCategoryId);
				return;
			}

			String categoryName = adminPointCategoryEntity.getPointCategoryName();

			// 1. Firebase Storage 카테고리 폴더 삭제
			firebaseStorageService.deletePointItemFolder(categoryName);

			// 2. 카테고리 삭제
			adminPointCategoryRepository.deleteById(pointCategoryId);
			logger.info("deletePointCategory: 카테고리 DB 삭제 완료. ID: {}", pointCategoryId);

		} catch (Exception e) {
			logger.error("deletePointCategory: 카테고리 삭제 중 예외 발생. ID: {}, 오류: {}", pointCategoryId, e.getMessage(), e);
		}
	}

	@Override
	public void updatePointCategory(AdminPointCategoryEntity adminPointCategoryEntity,
			List<AdminPointItemEntity> pointItemList) {
		Integer categoryId = adminPointCategoryEntity.getPointCategoryId();

		try {
			// 1. 기존 카테고리 조회
			AdminPointCategoryEntity updateCategory = adminPointCategoryRepository.findById(categoryId).orElse(null);

			if (updateCategory == null) {
				logger.warn("[카테고리 수정 실패] ID {}에 해당하는 카테고리를 찾을 수 없습니다.", categoryId);
				return;
			}

			String oldCategoryName = updateCategory.getPointCategoryName();
			String newCategoryName = adminPointCategoryEntity.getPointCategoryName();
			boolean categoryNameChange = !Objects.equals(newCategoryName, oldCategoryName);

			// 2. 카테고리 이름이 변경된 경우: Firebase Storage 파일 이동
			if (categoryNameChange) {
				logger.info("[카테고리 이름 변경 감지] '{}' → '{}'", oldCategoryName, newCategoryName);

				// 해당하는 카테고리에 포함된 물품 리스트
				List<AdminPointItemImgEntity> pointItemImgList = adminPointItemImgService
						.findByPointItemIdIn(pointItemList);
				pointItemImgList = firebaseStorageService.updateCategoryName(newCategoryName, oldCategoryName,
						pointItemImgList);
				logger.info("[Firebase Storage 업데이트 완료] 카테고리 파일 이동 완료");

				adminPointItemImgService.updatePointItemImgPathAndURL(pointItemImgList);
			}

			// 3. DB 업데이트
			String memberId = commonUserName.getUserName();
			updateCategory.setMemberId(memberId);
			updateCategory.setPointCategoryName(newCategoryName);
			updateCategory.setPointCategorySortOrder(adminPointCategoryEntity.getPointCategorySortOrder());

			logger.info("[DB 업데이트 완료] 카테고리 ID {} 정보가 성공적으로 업데이트되었습니다.", categoryId);

		} catch (Exception e) {
			logger.error("[카테고리 수정 중 오류 발생] ID {} 처리 중 예외 발생", adminPointCategoryEntity.getPointCategoryId(), e);
		}
	}

}