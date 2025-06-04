package com.mogatshoo.dev.point.detail.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;

public interface PointHistoryService {

	void pointHistorySave(PointHistoryEntity pointHistoryEntity);

	int fortunePointUseCount(String memberId);

	int checkAttendancePoint(String memberId);

	Page<PointHistoryEntity> findByMemberId(String memberId, Pageable pageable);

	void memberDelete(String memberId);
}
