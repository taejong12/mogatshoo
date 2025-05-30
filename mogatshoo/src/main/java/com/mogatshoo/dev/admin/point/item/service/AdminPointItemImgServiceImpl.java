package com.mogatshoo.dev.admin.point.item.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemImgRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemImgServiceImpl implements AdminPointItemImgService{

	@Autowired
	private AdminPointItemImgRepository adminPointItemImgRepository;
	
	@Override
	public List<AdminPointItemImgEntity> findByItemId(List<AdminPointItemEntity> AdminPointItemEntity) {
		
		List<AdminPointItemImgEntity> adminPointItemImgEntity = new ArrayList<>();
		
		for(AdminPointItemEntity pointItem :  AdminPointItemEntity) {
			long pointItemId = pointItem.getPointItemId();
			AdminPointItemImgEntity itemImg = adminPointItemImgRepository.findByPointItemId(pointItemId);
			adminPointItemImgEntity.add(itemImg);
		}
		
		return adminPointItemImgEntity;
	}

}
