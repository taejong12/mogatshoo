package com.mogatshoo.dev.admin.question.service;

import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface QuestionService {
	
	// 새 질문 생성
	QuestionEntity createQuestion(QuestionEntity questionEntity);
	
	// 다음 일련번호 생성
	String generateNextSerialNumber();
	
	// 모든 질문 조회
	List<QuestionEntity> getAllQuestions();
	
	// 일련번호로 질문 조회
	QuestionEntity getQuestionBySerialNumber(String serialNumber);
	
	// 공개 여부 변경 (관리자용)
	QuestionEntity updatePublicStatus(String serialNumber, String isPublic);
	QuestionEntity updateQuestion(QuestionEntity questionEntity);
	
	// 질문 삭제
	void deleteQuestion(String serialNumber);

	List<QuestionEntity> getQuestionsByPublicStatus(String string);
	
	// 이미지 경로 수정 메서드 추가
	List<QuestionEntity> getAllQuestionsWithFixedImagePaths();
	
	void updateExpiredVotingStatus();
    void archiveCompletedQuestions();
    
    Page<QuestionEntity> getQuestionsWithPaging(Pageable pageable);
    
    Page<QuestionEntity> searchQuestions(String keyword, String publicStatus, LocalDate createdDate, LocalDate votingDate, Pageable pageable);
}