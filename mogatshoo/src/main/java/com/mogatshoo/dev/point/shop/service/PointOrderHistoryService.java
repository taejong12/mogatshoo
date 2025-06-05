package com.mogatshoo.dev.point.shop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.point.shop.entity.PointOrderHistoryEntity;

public interface PointOrderHistoryService {

	void pointOrderHistorySave(PointOrderHistoryEntity pointOrderHistory);

	Page<PointOrderHistoryEntity> findByMemberId(String memberId, Pageable pageable);

}
