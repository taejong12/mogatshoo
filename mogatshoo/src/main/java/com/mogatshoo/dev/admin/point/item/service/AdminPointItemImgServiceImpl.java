package com.mogatshoo.dev.admin.point.item.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemImgRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemImgServiceImpl implements AdminPointItemImgService {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemImgServiceImpl.class);
	
	@Autowired
	private AdminPointItemImgRepository adminPointItemImgRepository;

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
	public void save(MultipartFile imgFile, Long itemId) {
		try {
			// 저장 폴더
			String uploadDir = "C:/upload/point_item_images/";
			File uploadFolder = new File(uploadDir);
			
			// 폴더가 존재하지 않으면 생성
			if (!uploadFolder.exists()) {
				boolean created = uploadFolder.mkdirs();
				if (created) {
					logger.info("폴더 생성 성공 - uploadDir: {}", uploadDir);
				} else {
					logger.error("폴더 생성 실패 - uploadDir: {}", uploadDir);
					throw new RuntimeException("업로드 폴더 생성 실패");
				}
			}
			
			String originalFilename = imgFile.getOriginalFilename();
			String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;

			File dest = new File(uploadDir + newFileName);
			// 실제 파일 저장
			imgFile.transferTo(dest);
			logger.info("이미지 저장 완료 - 파일명: {}, 경로: {}", newFileName, dest.getAbsolutePath());

			// DB에 이미지 정보 저장
			AdminPointItemImgEntity imgEntity = new AdminPointItemImgEntity();
			imgEntity.setPointItemId(itemId);
			imgEntity.setPointItemImgName(newFileName);
			imgEntity.setPointItemImgPath(uploadDir + newFileName);

			adminPointItemImgRepository.save(imgEntity);
			logger.info("DB 이미지 정보 저장 완료 - pointItemId: {}, 파일명: {}", itemId, newFileName);
		} catch (IOException e) {
			logger.error("이미지 저장 중 예외 발생 - {}", e.getMessage(), e);
			throw new RuntimeException("이미지 저장 실패");
		}
	}
}
