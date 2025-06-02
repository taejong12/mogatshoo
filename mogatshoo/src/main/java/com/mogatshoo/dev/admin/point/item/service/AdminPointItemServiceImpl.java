package com.mogatshoo.dev.admin.point.item.service;

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
import com.mogatshoo.dev.admin.point.category.service.AdminPointCategoryServiceImpl;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemRepository;
import com.mogatshoo.dev.common.authentication.CommonUserName;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemServiceImpl implements AdminPointItemService {

    private final AdminPointCategoryServiceImpl adminPointCategoryServiceImpl;

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemServiceImpl.class);

	@Autowired
	private AdminPointItemRepository adminPointItemRepository;

	@Autowired
	private AdminPointItemImgService adminPointItemImgService;
	
	@Autowired
	private AdminPointCategoryService adminPointCategoryService;
	
	@Autowired
	private CommonUserName commonUserName;

    AdminPointItemServiceImpl(AdminPointCategoryServiceImpl adminPointCategoryServiceImpl) {
        this.adminPointCategoryServiceImpl = adminPointCategoryServiceImpl;
    }

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

			if (!imgFile.isEmpty()) {
				Long pointItemId = adminPointItemEntity.getPointItemId();
				AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryService.findById(adminPointItemEntity.getPointCategoryId());
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
}
