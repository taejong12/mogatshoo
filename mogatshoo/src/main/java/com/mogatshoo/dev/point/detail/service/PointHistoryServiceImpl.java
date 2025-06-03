package com.mogatshoo.dev.point.detail.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.detail.service.PointHistoryService;
import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.detail.repository.PointHistoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointHistoryServiceImpl implements PointHistoryService {

	@Autowired
	private PointHistoryRepository pointHistoryRepository;

	@Override
	public void pointHistorySave(PointHistoryEntity pointHistoryEntity) {
		pointHistoryRepository.save(pointHistoryEntity);
	}

	@Override
	public int fortunePointUseCount(String memberId) {
		String reason = "운세";
		return countByMemberIdAndReasonAndPointCreateBetween(memberId, reason);
	}

	@Override
	public int checkAttendancePoint(String memberId) {
		String reason = "출석";
		return countByMemberIdAndReasonAndPointCreateBetween(memberId, reason);
	}

	public int countByMemberIdAndReasonAndPointCreateBetween(String memberId, String reason) {
		LocalDate today = LocalDate.now();
		LocalDateTime start = today.atStartOfDay();
		LocalDateTime end = today.plusDays(1).atStartOfDay().minusSeconds(1);
		return pointHistoryRepository.countByMemberIdAndReasonAndPointCreateBetween(memberId, reason, start, end);
	}

	@Override
	public Page<PointHistoryEntity> findByMemberId(String memberId, Pageable pageable) {
		return pointHistoryRepository.findByMemberId(memberId, pageable);
	}

	@Override
	public void memberDelete(String memberId) {
		pointHistoryRepository.deleteAllByMemberId(memberId);
	}

}
