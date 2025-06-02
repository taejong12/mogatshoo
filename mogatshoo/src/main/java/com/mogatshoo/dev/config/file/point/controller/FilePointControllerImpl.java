package com.mogatshoo.dev.config.file.point.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.config.file.point.service.FilePointService;

@Controller
public class FilePointControllerImpl implements FilePointController{

	@Autowired
	private FilePointService filePointService;

	@Override
	public void deletePointItemImg(AdminPointItemImgEntity oldImgEntity) {
		filePointService.deletePointItemImg(oldImgEntity);
	}

	@Override
	public String uploadFileToPointItem(MultipartFile imgFile, String newFileName) {
		return filePointService.uploadFileToPointItem(imgFile, newFileName);
	}

	@Override
	public String getFileUrl(String pointItemImgFileId) {
		return filePointService.getFileUrl(pointItemImgFileId);
	}
	
}
