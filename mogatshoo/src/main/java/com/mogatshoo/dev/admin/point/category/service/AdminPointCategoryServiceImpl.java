package com.mogatshoo.dev.admin.point.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointCategoryServiceImpl implements AdminPointCategoryService {

	@Override
	public List<AdminPointCategoryEntity> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

}
