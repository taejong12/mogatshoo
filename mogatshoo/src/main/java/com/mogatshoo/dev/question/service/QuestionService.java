package com.mogatshoo.dev.question.service;

import com.mogatshoo.dev.question.entity.QuestionEntity;
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
}