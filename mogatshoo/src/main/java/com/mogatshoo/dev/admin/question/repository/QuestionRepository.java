package com.mogatshoo.dev.admin.question.repository;

import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, String> {
    
    // 가장 최근의 일련번호 조회
    @Query("SELECT MAX(q.serialNumber) FROM question q")
    String findMaxSerialNumber();
    
    // 공개 상태별 질문 조회
    List<QuestionEntity> findByIsPublic(String isPublic);
    
    // 투표가 종료된 질문들 조회 (현재 시간이 종료 시간을 넘은 것들)
    @Query("SELECT q FROM question q WHERE q.votingEndDate < :currentTime AND q.votingStatus = '진행중'")
    List<QuestionEntity> findExpiredVotingQuestions(@Param("currentTime") LocalDateTime currentTime);
    
    // 종료된 지 하루가 지난 질문들 조회 (아카이빙 대상)
    @Query("SELECT q FROM question q WHERE q.votingEndDate < :oneDayAgo AND q.votingStatus = '종료'")
    List<QuestionEntity> findQuestionsForArchiving(@Param("oneDayAgo") LocalDateTime oneDayAgo);
    
    /**
     * 통합 검색 쿼리 - 모든 조건을 OR로 연결하여 검색
     */
    @Query("SELECT q FROM question q WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(q.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(q.question) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:publicStatus IS NULL OR :publicStatus = '' OR q.isPublic = :publicStatus) AND " +
           "(:createdDate IS NULL OR DATE(q.createdAt) = :createdDate) AND " +
           "(:votingDate IS NULL OR " +
           " (q.votingStartDate IS NOT NULL AND DATE(q.votingStartDate) <= :votingDate AND " +
           "  q.votingEndDate IS NOT NULL AND DATE(q.votingEndDate) >= :votingDate))")
    Page<QuestionEntity> searchQuestions(
            @Param("keyword") String keyword,
            @Param("publicStatus") String publicStatus,
            @Param("createdDate") LocalDate createdDate,
            @Param("votingDate") LocalDate votingDate,
            Pageable pageable);
}