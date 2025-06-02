package com.mogatshoo.dev.admin.point.item.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemImgRepository;
import com.mogatshoo.dev.config.file.point.controller.FilePointController;
import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.entity.StageEntity;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemImgServiceImpl implements AdminPointItemImgService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemImgServiceImpl.class);

	@Autowired
	private AdminPointItemImgRepository adminPointItemImgRepository;

	@Autowired
	private FilePointController filePointController;

	@Override
	public List<AdminPointItemImgEntity> findByItemId(List<AdminPointItemEntity> AdminPointItemEntity) {

		List<AdminPointItemImgEntity> adminPointItemImgEntity = new ArrayList<>();

		for (AdminPointItemEntity pointItem : AdminPointItemEntity) {
			long pointItemId = pointItem.getPointItemId();
			AdminPointItemImgEntity itemImg = adminPointItemImgRepository.findByPointItemId(pointItemId);
			adminPointItemImgEntity.add(itemImg);
		}

		return adminPointItemImgEntity;
	}

	@Override
	public void save(MultipartFile imgFile, Long pointItemId) {
		try {
			AdminPointItemImgEntity oldImgEntity = adminPointItemImgRepository.findByPointItemId(pointItemId);
			if (oldImgEntity != null && oldImgEntity.getPointItemImgName() != null) {
				// 기존 파일 삭제
				filePointController.deletePointItemImg(oldImgEntity);
			}

			// 이미지 업로드
			String originalFilename = imgFile.getOriginalFilename();
			String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;
			String pointItemImgFileId = filePointController.uploadFileToPointItem(imgFile, newFileName);
			String uploadDir = filePointController.getFileUrl(pointItemImgFileId);

			// DB에 이미지 정보 저장
			AdminPointItemImgEntity imgEntity = new AdminPointItemImgEntity();
			imgEntity.setPointItemId(pointItemId);
			imgEntity.setPointItemImgFileId(pointItemImgFileId);
			imgEntity.setPointItemImgName(newFileName);
			imgEntity.setPointItemImgPath(uploadDir);

			adminPointItemImgRepository.save(imgEntity);
			logger.info("DB 이미지 정보 저장 완료 - pointItemId: {}, 파일명: {}", pointItemId, newFileName);
		} catch (Exception e) {
			logger.error("이미지 저장 중 예외 발생 - {}", e.getMessage(), e);
			throw new RuntimeException("이미지 저장 실패");
		}
	}

	@Override
	public AdminPointItemImgEntity findByPointItemId(Long pointItemId) {
		try {
			AdminPointItemImgEntity entity = adminPointItemImgRepository.findByPointItemId(pointItemId);

			if (entity == null) {
				logger.warn("포인트 아이템 이미지가 존재하지 않습니다. pointItemId: {}", pointItemId);
			} else {
				logger.info("포인트 아이템 이미지 조회 성공. pointItemId: {}", pointItemId);
			}
			return entity;
		} catch (Exception e) {
			logger.error("포인트 아이템 이미지 조회 중 오류 발생. pointItemId: {}", pointItemId, e);
			return null;
		}
	}
}
