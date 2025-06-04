package com.mogatshoo.dev.point.detail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.detail.service.PointHistoryService;
import com.mogatshoo.dev.point.shop.service.PointShopServiceImpl;
import com.mogatshoo.dev.point.detail.entity.PointEntity;
import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.detail.repository.PointRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointServiceImpl implements PointService {

	private static final Logger logger = LoggerFactory.getLogger(PointServiceImpl.class);

	@Autowired
	private PointRepository pointRepository;

	@Autowired
	private PointHistoryService pointHistoryService;

	@Override
	public void memberJoinPointSave(String memberId) {
		try {
			int point = 300;

			// 포인트 저장
			PointEntity pointEntity = new PointEntity();
			pointEntity.setMemberId(memberId);
			pointEntity.setPoint(point);
			pointRepository.save(pointEntity);
			logger.info("회원 [{}] 포인트 [{}P] 적립 완료", memberId, point);

			// 포인트 적립 히스토리 저장
			PointHistoryEntity pointHistoryEntity = new PointHistoryEntity();
			pointHistoryEntity.setMemberId(memberId);
			pointHistoryEntity.setReason("회원가입");
			pointHistoryEntity.setType("적립");
			pointHistoryEntity.setChangePoint(point);
			pointHistoryService.pointHistorySave(pointHistoryEntity);
			logger.info("회원 [{}] 포인트 적립 히스토리 저장 완료", memberId);

		} catch (Exception e) {
			logger.error("회원 [{}] 포인트 적립 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("포인트 적립 중 오류가 발생했습니다.");
		}
	}

	@Override
	public int findByMemberId(String memberId) {
		try {
			PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);

			if (pointEntity == null) {
				logger.warn("회원 [{}]의 포인트 정보가 존재하지 않습니다. 기본값 0 반환", memberId);
				return 0;
			}

			int point = pointEntity.getPoint();
			logger.info("회원 [{}]의 보유 포인트 조회 완료: {}P", memberId, point);
			return point;

		} catch (Exception e) {
			logger.error("회원 [{}] 포인트 조회 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("포인트 조회 중 오류가 발생했습니다.");
		}
	}

	@Override
	public int fortunePointUse(String memberId) {

		try {
			// 오늘 이미 사용했는지 확인
			int count = pointHistoryService.fortunePointUseCount(memberId);

			PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);
			if (pointEntity == null) {
				logger.warn("회원 [{}]의 포인트 정보가 존재하지 않습니다.", memberId);
				throw new IllegalStateException("회원 포인트 정보가 존재하지 않습니다.");
			}

			int point = pointEntity.getPoint();

			if (count == 0) {
				int use = 10;

				if (point >= use) {
					int changePoint = point - use;
					pointEntity.setPoint(changePoint);

					// 포인트 사용 히스토리 저장
					PointHistoryEntity pointHistoryEntity = new PointHistoryEntity();
					pointHistoryEntity.setMemberId(memberId);
					pointHistoryEntity.setReason("운세");
					pointHistoryEntity.setType("사용");
					pointHistoryEntity.setChangePoint(use);
					pointHistoryService.pointHistorySave(pointHistoryEntity);

					logger.info("회원 [{}] 운세 이용으로 포인트 {}P 사용. 잔여 포인트: {}P", memberId, use, changePoint);
					return changePoint;
				} else {
					logger.warn("회원 [{}] 포인트 부족. 보유 포인트: {}, 필요 포인트: {}", memberId, point, use);
					throw new IllegalStateException("포인트가 부족합니다.");
				}
			}

			logger.info("회원 [{}]은 오늘 이미 운세 이용 포인트를 차감했으므로 추가 차감 없음", memberId);
			return point;

		} catch (Exception e) {
			logger.error("회원 [{}] 운세 포인트 사용 처리 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("운세 포인트 처리 중 오류가 발생했습니다.");
		}
	}

	@Override
	public void checkAttendancePoint(String memberId) {
		try {
			int count = pointHistoryService.checkAttendancePoint(memberId);
			PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);

			if (pointEntity == null) {
				logger.warn("출석 포인트 지급 실패: 회원 [{}]의 포인트 정보가 존재하지 않습니다.", memberId);
				throw new IllegalStateException("회원 포인트 정보가 존재하지 않습니다.");
			}

			int point = pointEntity.getPoint();

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

				logger.info("회원 [{}]에게 출석 포인트 {}P 지급 완료. 총 포인트: {}P", memberId, savePoint, totalPoint);
			} else {
				logger.info("회원 [{}]은 오늘 이미 출석 포인트를 지급받았습니다.", memberId);
			}

		} catch (Exception e) {
			logger.error("출석 포인트 지급 중 예외 발생 - 회원 [{}]: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("출석 포인트 지급 처리 중 오류가 발생했습니다.");
		}
	}

	@Override
	public void memberDelete(String memberId) {
		try {
			// 포인트 히스토리 먼저 삭제
			pointHistoryService.memberDelete(memberId);
			logger.info("회원 [{}]의 포인트 히스토리 삭제 완료", memberId);

			// 포인트 엔티티 삭제
			pointRepository.deleteById(memberId);
			logger.info("회원 [{}]의 포인트 정보 삭제 완료", memberId);

		} catch (Exception e) {
			logger.error("회원 [{}] 삭제 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("회원 삭제 처리 중 오류가 발생했습니다.");
		}
	}

	@Override
	public PointEntity findById(String memberId) {
		try {
			PointEntity pointEntity = pointRepository.findById(memberId).orElse(null);

			if (pointEntity == null) {
				logger.warn("회원 [{}]의 포인트 정보가 존재하지 않습니다.", memberId);
			} else {
				logger.info("회원 [{}]의 포인트 정보 조회 성공", memberId);
			}

			return pointEntity;

		} catch (Exception e) {
			logger.error("회원 [{}]의 포인트 정보 조회 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("포인트 정보 조회 중 오류가 발생했습니다.");
		}
	}
}
