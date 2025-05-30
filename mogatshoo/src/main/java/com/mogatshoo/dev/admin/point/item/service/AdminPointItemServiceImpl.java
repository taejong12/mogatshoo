package com.mogatshoo.dev.admin.point.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.repository.AdminPointItemRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemServiceImpl implements AdminPointItemService {

	@Autowired
	private AdminPointItemRepository adminPointItemRepository;

	@Override
	public Page<AdminPointItemEntity> findAll(Pageable pageable) {
		return adminPointItemRepository.findAll(pageable);
	}

	@Override
	public Page<AdminPointItemEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable) {
		return adminPointItemRepository.findByPointCategoryId(pointCategoryId, pageable);
	}
}
