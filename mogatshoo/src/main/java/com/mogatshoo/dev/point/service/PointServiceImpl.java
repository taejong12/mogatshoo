package com.mogatshoo.dev.point.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.entity.PointEntity;
import com.mogatshoo.dev.point.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.repository.PointRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointServiceImpl implements PointService {

	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private PointHistoryService pointHistoryService;

	@Override
	public void memberJoinPointSave(String memberId) {
		PointEntity pointEntity = new PointEntity();
		pointEntity.setMemberId(memberId);
		int point = 300;
		pointEntity.setPoint(300);
		pointRepository.save(pointEntity);

		PointHistoryEntity pointHistoryEntity = new PointHistoryEntity();
		pointHistoryEntity.setMemberId(memberId);
		pointHistoryEntity.setReason("회원가입");
		pointHistoryEntity.setType("적립");
		pointHistoryEntity.setChangePoint(point);
		pointHistoryService.pointHistorySave(pointHistoryEntity);
	}

	@Override
	public int findByMemberId(String memberId) {
		PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);

		if (pointEntity == null) {
			return 0;
		}

		return pointEntity.getPoint();
	}

	@Override
	public int fortunePointUse(String memberId) {

		// 만약 오늘 운세를 이미 1번 돌리는데 포인트 사용했다면 오늘 하루동안은 이후에 차감 안되게 하는 조건
		int count = pointHistoryService.fortunePointUseCount(memberId);

		PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);
		int point = pointEntity.getPoint();

		if (count == 0) {
			int use = 10;

			if (point >= use) {
				int changePoint = point - use;

				pointEntity.setPoint(changePoint);

				PointHistoryEntity pointHistoryEntity = new PointHistoryEntity();
				pointHistoryEntity.setMemberId(memberId);
				pointHistoryEntity.setReason("운세");
				pointHistoryEntity.setType("사용");
				pointHistoryEntity.setChangePoint(use);
				pointHistoryService.pointHistorySave(pointHistoryEntity);

				return changePoint;
			} else {
				throw new IllegalStateException("포인트가 부족합니다.");
			}
		}

		return point;
	}

	@Override
	public void checkAttendancePoint(String memberId) {
		int count = pointHistoryService.checkAttendancePoint(memberId);

		PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);
		int point = pointEntity.getPoint();

		// 출석 포인트 지급
		if (count == 0) {
			int savePoint = 10;
			int totalPoint = point + savePoint;

			pointEntity.setPoint(totalPoint);

			PointHistoryEntity pointHistoryEntity = new PointHistoryEntity();
			pointHistoryEntity.setMemberId(memberId);
			pointHistoryEntity.setReason("출석");
			pointHistoryEntity.setType("적립");
			pointHistoryEntity.setChangePoint(savePoint);
			pointHistoryService.pointHistorySave(pointHistoryEntity);
		}
	}
}
