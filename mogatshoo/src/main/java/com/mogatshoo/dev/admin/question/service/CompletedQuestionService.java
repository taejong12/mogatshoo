package com.mogatshoo.dev.admin.question.service;

import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface CompletedQuestionService {
    
    // 모든 완료된 질문 조회
    List<CompletedQuestionEntity> getAllCompletedQuestions();
    
    // 일련번호로 완료된 질문 조회
    CompletedQuestionEntity getCompletedQuestionBySerialNumber(String serialNumber);
    
    // 페이징된 완료된 질문 목록 조회
    Page<CompletedQuestionEntity> getCompletedQuestionsWithPaging(Pageable pageable);
    
    // 완료된 질문 검색
    Page<CompletedQuestionEntity> searchCompletedQuestions(String keyword, String publicStatus, 
                                                         LocalDate createdDate, LocalDate completedDate, 
                                                         Pageable pageable);
}