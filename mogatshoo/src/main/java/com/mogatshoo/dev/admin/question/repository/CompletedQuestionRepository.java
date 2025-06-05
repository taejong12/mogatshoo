// CompletedQuestionRepository.java에 추가
package com.mogatshoo.dev.admin.question.repository;

import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedQuestionRepository extends JpaRepository<CompletedQuestionEntity, String> {
    
    // 투표 기간 정보를 포함한 완료된 질문 조회
    @Query("SELECT cq FROM completed_question cq ORDER BY cq.createdAt DESC")
    List<CompletedQuestionEntity> findAllOrderByCreatedAtDesc();
    
    // 특정 조건으로 완료된 질문 검색
    @Query("SELECT cq FROM completed_question cq WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(cq.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(cq.question) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:publicStatus IS NULL OR :publicStatus = '' OR cq.isPublic = :publicStatus)")
    List<CompletedQuestionEntity> searchCompletedQuestions(
            @Param("keyword") String keyword,
            @Param("publicStatus") String publicStatus);
}