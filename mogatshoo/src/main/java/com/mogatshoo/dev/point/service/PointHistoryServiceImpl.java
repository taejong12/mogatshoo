package com.mogatshoo.dev.point.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.point.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.repository.PointHistoryRepository;

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
	
}
