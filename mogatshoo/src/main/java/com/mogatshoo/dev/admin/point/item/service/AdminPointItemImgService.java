package com.mogatshoo.dev.admin.point.item.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface AdminPointItemImgService {

	List<AdminPointItemImgEntity> findByItemId(List<AdminPointItemEntity> AdminPointItemEntity);

	void save(MultipartFile imgFile, Long itemId);

}
