package com.mogatshoo.dev.config.file.point.service;

import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface FilePointService {

	void deletePointItemImg(AdminPointItemImgEntity oldImgEntity);

	String uploadFileToPointItem(MultipartFile imgFile, String newFileName);

	String getFileUrl(String pointItemImgFileId);

}
