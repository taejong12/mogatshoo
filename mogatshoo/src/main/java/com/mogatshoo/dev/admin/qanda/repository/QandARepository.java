package com.mogatshoo.dev.admin.qanda.repository;
import com.mogatshoo.dev.admin.qanda.entity.QandAEntity;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QandARepository extends JpaRepository<QandAEntity, Long> {

	// 특정 채팅방의 메시지들을 시간순으로 조회
	List<QandAEntity> findByRoomIdOrderByCreatedAtAsc(String roomId);

	// 읽지 않은 메시지들 조회
	List<QandAEntity> findByRoomIdAndIsReadFalse(String roomId);

	// 모든 채팅방의 읽지 않은 메시지 개수
	@Query("SELECT m.roomId, COUNT(m) FROM QandAEntity m WHERE m.isRead = false GROUP BY m.roomId")
	List<Object[]> countUnreadMessagesByRoom();

	// 활성 채팅방 목록 (메시지가 있는 방들) - 수정됨
	@Query("SELECT m.roomId FROM QandAEntity m GROUP BY m.roomId ORDER BY MAX(m.createdAt) DESC")
	List<String> findActiveRooms();

	long countByCreatedAtBefore(LocalDateTime cutoffDate);

	@Modifying
	@Transactional
	@Query("DELETE FROM QandAEntity q WHERE q.createdAt < :cutoffDate")
	int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

	// 선택적: 특정 기간의 메시지만 조회 (성능 최적화)
	@Query("SELECT q FROM QandAEntity q WHERE q.createdAt >= :fromDate ORDER BY q.createdAt ASC")
	List<QandAEntity> findMessagesAfter(@Param("fromDate") LocalDateTime fromDate);
}
