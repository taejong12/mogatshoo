package com.mogatshoo.dev.point.shop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.shop.entity.PointOrderHistoryEntity;
import com.mogatshoo.dev.point.shop.repository.PointOrderHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointOrderHistoryServiceImpl implements PointOrderHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(PointOrderHistoryServiceImpl.class);

	@Autowired
	private PointOrderHistoryRepository pointOrderHistoryRepository;

	@Override
	public void pointOrderHistorySave(PointOrderHistoryEntity pointOrderHistory) {
		try {
			pointOrderHistoryRepository.save(pointOrderHistory);
		} catch (Exception e) {
			logger.error("포인트 상품 구매내역 저장 중 오류 발생 - 데이터: {}", pointOrderHistory, e);
			throw e;
		}
	}
}
