package com.mogatshoo.dev.question.service;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.repository.PictureRepository;
import com.mogatshoo.dev.question.entity.QuestionEntity;
import com.mogatshoo.dev.question.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
	
	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	PictureRepository picRepository;
	
	@Override
	public QuestionEntity createQuestion(QuestionEntity questionEntity) {
		// 질문 생성 전 다음 일련번호 설정
		String nextSerialNumber = generateNextSerialNumber();
		questionEntity.setSerialNumber(nextSerialNumber);
		
		// 기본값 설정
		questionEntity.setIsPublic("no");
		
		return questionRepository.save(questionEntity);
	}
	
	
	
	@Override
	public String generateNextSerialNumber() {
		String maxSerialNumber = questionRepository.findMaxSerialNumber();
		
		if (maxSerialNumber == null) {
			// 첫 번째 질문인 경우 시작 번호 설정 (예: "Q0001")
			return "Q0001";
		} else {
			// 기존 일련번호에서 숫자 부분 추출
			String numericPart = maxSerialNumber.substring(1);
			int numericValue = Integer.parseInt(numericPart);
			
			// +1 증가
			numericValue++;
			
			// 동일한 형식으로 포맷팅 (예: "Q0002", "Q0010", "Q0100")
			return String.format("Q%04d", numericValue);
		}
	}
	
	@Override
	public List<QuestionEntity> getAllQuestions() {
		return questionRepository.findAll();
	}
	
	@Override
	public QuestionEntity getQuestionBySerialNumber(String serialNumber) {
		return questionRepository.findById(serialNumber).orElseThrow(() -> new RuntimeException("Question not found with serial number: " + serialNumber));
	}
	
	@Override
	public QuestionEntity updatePublicStatus(String serialNumber, String isPublic) {
		QuestionEntity question = getQuestionBySerialNumber(serialNumber);
		question.setIsPublic(isPublic);
		return questionRepository.save(question);
	}

	@Override
	public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
	    // 기존 질문 조회
	    QuestionEntity existingQuestion = getQuestionBySerialNumber(questionEntity.getSerialNumber());
	    
	    // 질문과 보기 업데이트
	    existingQuestion.setQuestion(questionEntity.getQuestion());
	    existingQuestion.setOption1(questionEntity.getOption1());
	    existingQuestion.setOption2(questionEntity.getOption2());
	    existingQuestion.setOption3(questionEntity.getOption3());
	    existingQuestion.setOption4(questionEntity.getOption4());
	    
	    // isPublic 상태도 업데이트하도록 추가
	    existingQuestion.setIsPublic(questionEntity.getIsPublic());
	    
	    // 저장 및 반환
	    return questionRepository.save(existingQuestion);
	}
	
	@Override
	public void deleteQuestion(String serialNumber) {
		// 질문 존재 여부 확인
		QuestionEntity question = getQuestionBySerialNumber(serialNumber);
		
		// 존재하면 삭제
		if (question != null) {
			questionRepository.delete(question);
		} else {
			throw new RuntimeException("삭제할 질문을 찾을 수 없습니다: " + serialNumber);
		}
	}
}