package com.mogatshoo.dev.point.detail.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.detail.service.PointHistoryService;
import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.detail.repository.PointHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointHistoryServiceImpl implements PointHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(PointHistoryServiceImpl.class);

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Override
	public void pointHistorySave(PointHistoryEntity pointHistoryEntity) {
		try {
			pointHistoryRepository.save(pointHistoryEntity);
			logger.info("포인트 내역 저장 완료 - 회원: {}, 사유: {}, 변경포인트: {}", pointHistoryEntity.getMemberId(),
					pointHistoryEntity.getReason(), pointHistoryEntity.getChangePoint());
		} catch (Exception e) {
			logger.error("포인트 내역 저장 실패 - 회원: {}, 사유: {}, 오류: {}", pointHistoryEntity.getMemberId(),
					pointHistoryEntity.getReason(), e.getMessage(), e);
			throw new RuntimeException("포인트 내역 저장 중 오류가 발생했습니다.");
		}
	}

	@Override
	public int fortunePointUseCount(String memberId) {
		String reason = "운세";

		try {
			int count = countByMemberIdAndReasonAndPointCreateBetween(memberId, reason);
			logger.info("회원 [{}]의 오늘 '{}' 포인트 사용 횟수: {}", memberId, reason, count);
			return count;
		} catch (Exception e) {
			logger.error("회원 [{}]의 '{}' 포인트 사용 횟수 조회 중 오류 발생: {}", memberId, reason, e.getMessage(), e);
			throw new RuntimeException("포인트 사용 기록 조회 중 오류가 발생했습니다.");
		}
	}

	@Override
	public int checkAttendancePoint(String memberId) {
		String reason = "출석";

		try {
			int count = countByMemberIdAndReasonAndPointCreateBetween(memberId, reason);
			logger.info("회원 [{}]의 오늘 '{}' 포인트 적립 횟수: {}", memberId, reason, count);
			return count;
		} catch (Exception e) {
			logger.error("회원 [{}]의 '{}' 포인트 적립 횟수 조회 중 오류 발생: {}", memberId, reason, e.getMessage(), e);
			throw new RuntimeException("출석 포인트 기록 조회 중 오류가 발생했습니다.");
		}
	}

	public int countByMemberIdAndReasonAndPointCreateBetween(String memberId, String reason) {
		LocalDate today = LocalDate.now();
		LocalDateTime start = today.atStartOfDay();
		LocalDateTime end = today.plusDays(1).atStartOfDay().minusSeconds(1);
		return pointHistoryRepository.countByMemberIdAndReasonAndPointCreateBetween(memberId, reason, start, end);
	}

	@Override
	public Page<PointHistoryEntity> findByMemberId(String memberId, Pageable pageable) {
		try {
			Page<PointHistoryEntity> page = pointHistoryRepository.findByMemberId(memberId, pageable);
			logger.info("회원 [{}]의 포인트 내역 조회 완료 - 조회 개수: {}", memberId, page.getNumberOfElements());
			return page;
		} catch (Exception e) {
			logger.error("회원 [{}]의 포인트 내역 조회 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("포인트 내역 조회 중 오류가 발생했습니다.");
		}
	}

	@Override
	public void memberDelete(String memberId) {
		try {
			pointHistoryRepository.deleteAllByMemberId(memberId);
			logger.info("회원 [{}]의 포인트 내역이 모두 삭제되었습니다.", memberId);
		} catch (Exception e) {
			logger.error("회원 [{}]의 포인트 내역 삭제 중 오류 발생: {}", memberId, e.getMessage(), e);
			throw new RuntimeException("포인트 내역 삭제 중 오류가 발생했습니다.");
		}
	}
}
