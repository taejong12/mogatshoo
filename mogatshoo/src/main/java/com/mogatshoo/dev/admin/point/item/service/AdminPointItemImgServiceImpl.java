package com.mogatshoo.dev.admin.point.item.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemImgRepository;
import com.mogatshoo.dev.config.file.FirebaseStorageService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemImgServiceImpl implements AdminPointItemImgService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemImgServiceImpl.class);

	@Autowired
	private AdminPointItemImgRepository adminPointItemImgRepository;

	@Autowired
	private FirebaseStorageService firebaseStorageService;

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
	public void save(MultipartFile imgFile, String pointCategoryName, Long pointItemId) {
		try {
			AdminPointItemImgEntity oldImgEntity = adminPointItemImgRepository.findByPointItemId(pointItemId);
			if (oldImgEntity != null && oldImgEntity.getPointItemImgName() != null) {
				// 기존 파일 삭제 (Firebase Storage Path 사용)
				firebaseStorageService.deletePointItemImg(oldImgEntity.getPointItemImgPath());
			}

			// 이미지 업로드
			String originalFilename = imgFile.getOriginalFilename();
			String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;
			String firebaseStoragePath = firebaseStorageService.uploadFileToPointItem(imgFile, pointCategoryName,
					newFileName);
			String firebaseStorageUrl = firebaseStorageService.getFileUrl(firebaseStoragePath);

			// DB에 이미지 정보 저장
			AdminPointItemImgEntity imgEntity = new AdminPointItemImgEntity();
			imgEntity.setPointItemId(pointItemId);
			imgEntity.setPointItemImgURL(firebaseStorageUrl);
			imgEntity.setPointItemImgName(newFileName);
			imgEntity.setPointItemImgPath(firebaseStoragePath);

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

	@Override
	public void deletePointItemImg(Long pointItemId) {

		try {
			AdminPointItemImgEntity adminPointItemImgEntity = adminPointItemImgRepository
					.findByPointItemId(pointItemId);
			if (adminPointItemImgEntity != null) {
				// Firebase Storage 이미지 삭제
				if (adminPointItemImgEntity != null && adminPointItemImgEntity.getPointItemImgPath() != null
						&& firebaseStorageService.isEnabled()) {
					firebaseStorageService.deletePointItemImg(adminPointItemImgEntity.getPointItemImgPath());
				}
				// DB 이미지 삭제
				adminPointItemImgRepository.deleteById(adminPointItemImgEntity.getPointItemImgId());
				logger.info("포인트 아이템 이미지 삭제 완료 - PointItemImgId: {}", adminPointItemImgEntity.getPointItemImgId());
			} else {
				logger.warn("해당 포인트 아이템에 대한 이미지가 존재하지 않음 - pointItemId: {}", pointItemId);
			}
		} catch (Exception e) {
			logger.error("포인트 아이템 이미지 삭제 중 오류 발생 - pointItemId: {}, error: {}", pointItemId, e.getMessage(), e);
		}
	}

	@Override
	public void updatePointItemImg(MultipartFile imgFile, String pointCategoryName, Long pointItemId) {

		try {
			// 기존 이미지 삭제
			deletePointItemImg(pointItemId);
			logger.info("기존 이미지 삭제 완료. pointItemId: {}", pointItemId);

			// 새 이미지 등록
			save(imgFile, pointCategoryName, pointItemId);
			logger.info("새 이미지 저장 완료. pointItemId: {}, 카테고리: {}", pointItemId, pointCategoryName);
		} catch (Exception e) {
			logger.error("이미지 업데이트 중 예외 발생. pointItemId: {}, 메시지: {}", pointItemId, e.getMessage(), e);
		}
	}

	@Override
	public void moveImgToNewCategory(Long pointItemId, String oldCategoryName, String newCategoryName) {
		try {
			// 1. 기존 이미지 정보 조회
			AdminPointItemImgEntity imgEntity = adminPointItemImgRepository.findByPointItemId(pointItemId);

			if (imgEntity == null) {
				logger.warn("moveImgToNewCategory: 이미지 정보가 존재하지 않음. ID: {}", pointItemId);
				return;
			}

			// Firebase Storage 파일 경로
			String filePath = imgEntity.getPointItemImgPath();

			if (filePath == null) {
				logger.warn("moveImgToNewCategory: 이미지 파일 경로가 존재하지 않음. ID: {}", pointItemId);
				return;
			}

			// 2. 파일 이동 (Firebase Storage에서는 파일 경로로 이동)
			firebaseStorageService.moveImgToNewCategory(filePath, newCategoryName, oldCategoryName);

			// 3. DB 업데이트 - 새로운 파일 경로로 업데이트
			String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
			String newFilePath = "point-items/" + newCategoryName + "/" + fileName;
			String newFileUrl = firebaseStorageService.getFileUrl(newFilePath);

			imgEntity.setPointItemImgPath(newFilePath);
			imgEntity.setPointItemImgURL(newFileUrl);
			adminPointItemImgRepository.save(imgEntity);

			logger.info("이미지 카테고리 이동 및 DB 업데이트 완료. ID: {}, {} → {}", pointItemId, oldCategoryName, newCategoryName);

		} catch (Exception e) {
			logger.error("이미지 카테고리 이동 실패. ID: {}, {} → {}", pointItemId, oldCategoryName, newCategoryName, e);
		}
	}

	@Override
	public boolean pointCategoryImgCheck(String pointCategoryName) {
		return firebaseStorageService.pointCategoryImgCheck(pointCategoryName);
	}

	@Override
	public void updatePointItemImgPathAndURL(List<AdminPointItemImgEntity> pointItemImgList) {
		for (AdminPointItemImgEntity newImgEntity : pointItemImgList) {
			try {
				Long imgId = newImgEntity.getPointItemImgId();
				System.out.println("imgId: "+imgId);
				adminPointItemImgRepository.findById(imgId).ifPresentOrElse(oldImgEntity -> {
					oldImgEntity.setPointItemImgPath(newImgEntity.getPointItemImgPath());
					oldImgEntity.setPointItemImgURL(newImgEntity.getPointItemImgURL());

					logger.debug("이미지 정보 업데이트 완료 - ID: {}, Path: {}, URL: {}", imgId,
							newImgEntity.getPointItemImgPath(), newImgEntity.getPointItemImgURL());
				}, () -> {
					logger.warn("이미지 ID [{}]에 해당하는 엔티티를 찾을 수 없습니다.", imgId);
				});

			} catch (Exception e) {
				logger.error("이미지 정보 업데이트 중 예외 발생 - ID: {}", newImgEntity.getPointItemImgId(), e);
			}
		}
	}

	@Override
	public List<AdminPointItemImgEntity> findByPointItemIdIn(List<AdminPointItemEntity> pointItemList) {
		try {
			List<Long> pointItemIdList = pointItemList.stream().map(AdminPointItemEntity::getPointItemId)
					.collect(Collectors.toList());

			logger.debug("조회할 포인트 아이템 ID 목록: {}", pointItemIdList);

			List<AdminPointItemImgEntity> resultList = adminPointItemImgRepository.findByPointItemIdIn(pointItemIdList);

			logger.debug("조회된 이미지 엔티티 수: {}", resultList.size());

			return resultList;
		} catch (Exception e) {
			logger.error("포인트 아이템 이미지 조회 중 오류 발생", e);
			// 에러 발생 시 빈 리스트 반환
			return Collections.emptyList();
		}
	}
}