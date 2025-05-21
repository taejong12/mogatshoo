package com.mogatshoo.dev.point.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.point.entity.PointHistoryEntity;

public interface PointHistoryRepository extends JpaRepository<PointHistoryEntity, Integer> {

	int countByMemberIdAndReasonAndPointCreateBetween(String memberId, String reason, LocalDateTime start, LocalDateTime end);

	Page<PointHistoryEntity> findByMemberId(String memberId, Pageable pageable);

	void deleteAllByMemberId(String memberId);
}
