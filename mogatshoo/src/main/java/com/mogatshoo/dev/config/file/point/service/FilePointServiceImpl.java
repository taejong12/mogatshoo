package com.mogatshoo.dev.config.file.point.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.config.file.GoogleDriveService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FilePointServiceImpl implements FilePointService {

	@Autowired
	private GoogleDriveService googleDriveService;
	
	@Override
	public void deletePointItemImg(AdminPointItemImgEntity oldImgEntity) {
		// 구글 드라이브 파일 삭제용
		try {
			if (oldImgEntity != null && oldImgEntity.getPointItemImgFileId() != null && googleDriveService.isEnabled()) {
				googleDriveService.deletePointItemImg(oldImgEntity.getPointItemImgFileId());
			}
		} catch (Exception e) {
			System.err.println("구글 드라이브 파일 삭제 실패: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public String uploadFileToPointItem(MultipartFile imgFile, String pointCategoryName, String newFileName) {
		return googleDriveService.uploadFileToPointItem(imgFile, pointCategoryName, newFileName);
	}


	@Override
	public String getFileUrl(String pointItemImgFileId) {
		return googleDriveService.getFileUrl(pointItemImgFileId);
	}
}
