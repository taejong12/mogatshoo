package com.mogatshoo.dev.admin.point.item.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;
import com.mogatshoo.dev.admin.point.category.service.AdminPointCategoryService;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemRepository;
import com.mogatshoo.dev.common.authentication.CommonUserName;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemServiceImpl implements AdminPointItemService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemServiceImpl.class);

	@Autowired
	private AdminPointItemRepository adminPointItemRepository;

	@Autowired
	private AdminPointItemImgService adminPointItemImgService;

	@Autowired
	private AdminPointCategoryService adminPointCategoryService;

	// 스프링 시큐리티 세션 유저정보 공통함수
	@Autowired
	private CommonUserName commonUserName;

	@Override
	public Page<AdminPointItemEntity> findAll(Pageable pageable) {
		return adminPointItemRepository.findAll(pageable);
	}

	@Override
	public Page<AdminPointItemEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable) {
		return adminPointItemRepository.findByPointCategoryId(pointCategoryId, pageable);
	}

	@Override
	public void save(AdminPointItemEntity adminPointItemEntity) {
		try {
			String memberId = commonUserName.getUserName();
			adminPointItemEntity.setMemberId(memberId);

			// 저장 후 반환된 엔티티에서 ID 추출
			adminPointItemEntity = adminPointItemRepository.save(adminPointItemEntity);
			logger.info("포인트 아이템 저장 성공 - ID: {}", adminPointItemEntity.getPointItemId());

			MultipartFile imgFile = adminPointItemEntity.getImgFile();

			if (imgFile != null && !imgFile.isEmpty()) {
				Long pointItemId = adminPointItemEntity.getPointItemId();
				AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryService
						.findById(adminPointItemEntity.getPointCategoryId());
				String pointCategoryName = adminPointCategoryEntity.getPointCategoryName();
				adminPointItemImgService.save(imgFile, pointCategoryName, pointItemId);
				logger.info("이미지 저장 완료 - 파일명: {}, 아이템 ID: {}", imgFile.getOriginalFilename(), pointItemId);
			}
		} catch (Exception e) {
			logger.error("포인트 아이템 저장 중 오류 발생", e);
		}
	}

	@Override
	public AdminPointItemEntity findById(Long pointItemId) {
		try {
			Optional<AdminPointItemEntity> optionalEntity = adminPointItemRepository.findById(pointItemId);

			if (optionalEntity.isPresent()) {
				logger.info("포인트 아이템 조회 성공. pointItemId: {}", pointItemId);
				return optionalEntity.get();
			} else {
				logger.warn("포인트 아이템이 존재하지 않습니다. pointItemId: {}", pointItemId);
				return null;
			}

		} catch (Exception e) {
			logger.error("포인트 아이템 조회 중 오류 발생. pointItemId: {}", pointItemId, e);
			return null;
		}
	}

	@Override
	public void deletePointItem(Long pointItemId) {

		try {
			// 1. 이미지 삭제 (구글 드라이브), DB 삭제
			adminPointItemImgService.deletePointItemImg(pointItemId);

			// 2. 포인트 물품 삭제
			adminPointItemRepository.deleteById(pointItemId);
			logger.info("포인트 아이템 정보 삭제 완료 - pointItemId: {}", pointItemId);

		} catch (Exception e) {
			logger.error("포인트 아이템 삭제 중 오류 발생 - pointItemId: {}, error: {}", pointItemId, e.getMessage(), e);
		}

	}

	@Override
	public void updatePointItem(AdminPointItemEntity adminPointItemEntity) {

		try {
			// 1. 이미지 수정 (구글드라이브), DB 수정
			MultipartFile imgFile = adminPointItemEntity.getImgFile();
			if (imgFile != null && !imgFile.isEmpty()) {
				Long pointItemId = adminPointItemEntity.getPointItemId();
				AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryService
						.findById(adminPointItemEntity.getPointCategoryId());
				String pointCategoryName = adminPointCategoryEntity.getPointCategoryName();
				adminPointItemImgService.updatePointItemImg(imgFile, pointCategoryName, pointItemId);
			}

			// 2. 포인트 물품 수정
			Long pointItemId = adminPointItemEntity.getPointItemId();
			if (pointItemId == null) {
				logger.warn("updatePointItem: pointItemId가 null입니다. 업데이트 중단.");
				return;
			}

			AdminPointItemEntity oldPointItemEntity = adminPointItemRepository.findById(pointItemId).orElse(null);

			if (oldPointItemEntity != null) {
				boolean categoryChanged = !Objects.equals(adminPointItemEntity.getPointCategoryId(),
						oldPointItemEntity.getPointCategoryId());

				// 2-1. 이미지 수정 안 했고, 카테고리만 바뀐 경우 → 이미지 이동
				if ((imgFile == null || imgFile.isEmpty()) && categoryChanged) {
					String oldCategoryName = adminPointCategoryService.findById(oldPointItemEntity.getPointCategoryId())
							.getPointCategoryName();
					String newCategoryName = adminPointCategoryService
							.findById(adminPointItemEntity.getPointCategoryId()).getPointCategoryName();
					adminPointItemImgService.moveImgToNewCategory(pointItemId, oldCategoryName, newCategoryName);
					logger.info("이미지를 새로운 카테고리로 이동 완료. ID: {}, {} → {}", pointItemId, oldCategoryName, newCategoryName);
				}

				// 2-2. 나머지 정보 수정
				String memberId = commonUserName.getUserName();
				oldPointItemEntity.setMemberId(memberId);
				oldPointItemEntity.setPointItemName(adminPointItemEntity.getPointItemName());
				oldPointItemEntity.setPointItemDescription(adminPointItemEntity.getPointItemDescription());
				oldPointItemEntity.setPointItemPrice(adminPointItemEntity.getPointItemPrice());
				oldPointItemEntity.setPointItemStock(adminPointItemEntity.getPointItemStock());
				oldPointItemEntity.setPointItemSaleStatus(adminPointItemEntity.getPointItemSaleStatus());
				oldPointItemEntity.setPointCategoryId(adminPointItemEntity.getPointCategoryId());

				logger.info("포인트 상품이 성공적으로 수정되었습니다. ID: {}", pointItemId);
			} else {
				logger.warn("updatePointItem: 해당 ID의 포인트 상품이 존재하지 않습니다. ID: {}", pointItemId);
			}

		} catch (Exception e) {
			logger.error("updatePointItem 중 예외 발생: {}", e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Object> deletePointCategoryCheck(Integer pointCategoryId) {
		Map<String, Object> result = new HashMap<>();

		try {
			// 1. 카테고리 정보 조회
			AdminPointCategoryEntity category = adminPointCategoryService.findById(pointCategoryId);
			String categoryName = category.getPointCategoryName();

			// 2. 해당 카테고리 폴더 내 이미지 존재 여부 확인
			boolean hasImg = adminPointItemImgService.pointCategoryImgCheck(categoryName);

			if (hasImg) {
				// 이미지가 존재할 경우 삭제 불가 처리
				result.put("hasItems", true);
				logger.warn("카테고리 '{}'에 이미지 파일이 존재하여 삭제할 수 없습니다.", categoryName);
				result.put("message", "'" + categoryName + "' 카테고리에 이미지 파일이 있어 삭제할 수 없습니다.");
			} else {
				// 이미지가 없으면 DB에 상품 존재 여부 확인
				long itemCount = adminPointItemRepository.countByPointCategoryId(pointCategoryId);
				logger.info("카테고리 ID {}에 포함된 상품 수: {}", pointCategoryId, itemCount);

				if (itemCount > 0) {
					result.put("hasItems", true);
					result.put("message", "'" + categoryName + "' 카테고리에 등록된 상품이 있어 삭제할 수 없습니다.");
				} else {
					result.put("hasItems", false);
					result.put("message", "'" + categoryName + "' 카테고리는 삭제 가능합니다.");
				}
			}
		} catch (Exception e) {
			logger.error("카테고리 삭제 가능 여부 확인 중 오류 발생. ID: {}", pointCategoryId, e);
			result.put("message", "카테고리 삭제 확인 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
		}

		return result;
	}
}
