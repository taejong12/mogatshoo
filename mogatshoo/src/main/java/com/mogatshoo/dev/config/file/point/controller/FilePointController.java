package com.mogatshoo.dev.config.file.point.controller;

import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface FilePointController {

	void deletePointItemImg(AdminPointItemImgEntity oldImgEntity);

	String uploadFileToPointItem(MultipartFile imgFile, String pointCategoryName, String newFileName);

	String getFileUrl(String pointItemImgFileId);
}
