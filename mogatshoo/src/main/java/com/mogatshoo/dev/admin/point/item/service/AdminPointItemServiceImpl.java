package com.mogatshoo.dev.admin.point.item.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

			if (!imgFile.isEmpty()) {
				Long itemId = adminPointItemEntity.getPointItemId();
				adminPointItemImgService.save(imgFile, itemId);
				logger.info("이미지 저장 완료 - 파일명: {}, 아이템 ID: {}", imgFile.getOriginalFilename(), itemId);
			}
		} catch (Exception e) {
			logger.error("포인트 아이템 저장 중 오류 발생", e);
		}
	}
}
