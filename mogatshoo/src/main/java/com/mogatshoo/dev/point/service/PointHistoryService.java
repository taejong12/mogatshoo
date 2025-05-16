package com.mogatshoo.dev.point.service;

import com.mogatshoo.dev.point.entity.PointHistoryEntity;

public interface PointHistoryService {

	void pointHistorySave(PointHistoryEntity pointHistoryEntity);

	int fortunePointUseCount(String memberId);

	int checkAttendancePoint(String memberId);
}
