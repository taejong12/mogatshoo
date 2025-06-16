package com.mogatshoo.dev.admin.point.send.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemServiceImpl;
import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;
import com.mogatshoo.dev.admin.point.send.repository.AdminPointItemSendRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemSendServiceImpl implements AdminPointItemSendService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemSendServiceImpl.class);

	@Autowired
	private AdminPointItemSendRepository adminPointItemSendRepository;

	@Override
	public Page<AdminPointItemSendEntity> findAll(Pageable pageable) {
		try {
			Page<AdminPointItemSendEntity> page = adminPointItemSendRepository.findAll(pageable);
			logger.info("포인트상품 구매내역 전체 조회 성공: 총 {}건", page.getTotalElements());
			return page;
		} catch (Exception e) {
			logger.error("포인트상품 구매내역 전체 조회 중 오류 발생", e);
			return Page.empty();
		}
	}

	@Override
	public Page<AdminPointItemSendEntity> findByPointItemSendCheck(String pointItemSendCheck, Pageable pageable) {
		return adminPointItemSendRepository.findByPointItemSendCheck(pointItemSendCheck, pageable);
	}
}
