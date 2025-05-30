package com.mogatshoo.dev.admin.point.item.service;

import java.util.List;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface AdminPointItemImgService {

	List<AdminPointItemImgEntity> findByItemId(List<AdminPointItemEntity> AdminPointItemEntity);

}
