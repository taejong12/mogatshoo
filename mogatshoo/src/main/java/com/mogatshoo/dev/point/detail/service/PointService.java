package com.mogatshoo.dev.point.detail.service;

import com.mogatshoo.dev.point.detail.entity.PointEntity;

public interface PointService {

	void memberJoinPointSave(String memberId);

	int findByMemberId(String memberId);

	int fortunePointUse(String memberId);

	void checkAttendancePoint(String memberId);

	void memberDelete(String memberId);

	PointEntity findById(String memberId);
}
