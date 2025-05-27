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
	public List<QuestionEntity> getAllQuestionsWithFixedImagePaths() {
		// 모든 질문을 가져온 후 이미지 경로가 포함된 경우 수정
		List<QuestionEntity> questions = questionRepository.findAll();
		
		// 각 질문의 옵션에서 잘못된 이미지 경로 수정
		for (QuestionEntity question : questions) {
			question.setOption1(fixImagePath(question.getOption1()));
			question.setOption2(fixImagePath(question.getOption2()));
			question.setOption3(fixImagePath(question.getOption3()));
			question.setOption4(fixImagePath(question.getOption4()));
		}
		
		return questions;
	}
	
	// 이미지 경로 수정 헬퍼 메서드
	private String fixImagePath(String optionText) {
		if (optionText != null && optionText.contains("/uploads/")) {
			// 기존 경로: /uploads/qwer/79e6ba0d-9df3-47ad-9df1-b2ad89560aed.png
			// 수정 경로: /uploads/qwer/79e6ba0d-9df3-47ad-9df1-b2ad89560aed.png (동일하게 유지)
			// 만약 경로에 문제가 있다면 여기서 수정
			
			// HairLossTestService의 saveImageFile 메서드와 일치하도록 경로 검증
			if (!optionText.startsWith("/uploads/")) {
				// /uploads/로 시작하지 않는 경우 추가
				if (optionText.startsWith("uploads/")) {
					optionText = "/" + optionText;
				}
			}
		}
		return optionText;
	}
	
	@Override
	public QuestionEntity getQuestionBySerialNumber(String serialNumber) {
		System.out.println("=== Service 호출 ===");
		System.out.println("조회할 serialNumber: '" + serialNumber + "'");
		System.out.println("serialNumber 길이: " + serialNumber.length());
		System.out.println("serialNumber 바이트: " + java.util.Arrays.toString(serialNumber.getBytes()));
		
		// 데이터베이스에 있는 모든 질문의 일련번호 확인
		List<QuestionEntity> allQuestions = questionRepository.findAll();
		System.out.println("=== 데이터베이스에 있는 모든 질문 일련번호 ===");
		for (QuestionEntity q : allQuestions) {
			System.out.println("DB에 있는 serialNumber: '" + q.getSerialNumber() + "'");
		}
		
		QuestionEntity question = questionRepository.findById(serialNumber)
			.orElseThrow(() -> {
				System.err.println("질문을 찾을 수 없습니다: '" + serialNumber + "'");
				return new RuntimeException("Question not found with serial number: " + serialNumber);
			});
		
		System.out.println("✅ DB에서 조회 성공!");
		System.out.println("조회된 질문 정보:");
		System.out.println("- 일련번호: " + question.getSerialNumber());
		System.out.println("- 질문 내용: " + question.getQuestion());
		System.out.println("- 공개상태: " + question.getIsPublic());
		System.out.println("- 옵션1: " + question.getOption1());
		System.out.println("- 옵션2: " + question.getOption2());
		
		// 이미지 경로 수정
		question.setOption1(fixImagePath(question.getOption1()));
		question.setOption2(fixImagePath(question.getOption2()));
		question.setOption3(fixImagePath(question.getOption3()));
		question.setOption4(fixImagePath(question.getOption4()));
		
		System.out.println("✅ 이미지 경로 수정 완료, Service 종료");
		return question;
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
	
	@Override
	public List<QuestionEntity> getQuestionsByPublicStatus(String isPublic) {
	    try {
	        // QuestionRepository의 findByIsPublic 메소드를 호출하여 공개 상태에 따른 질문 목록 반환
	        List<QuestionEntity> questions = questionRepository.findByIsPublic(isPublic);
	        
	        // 이미지 경로 수정
	        for (QuestionEntity question : questions) {
	        	question.setOption1(fixImagePath(question.getOption1()));
	        	question.setOption2(fixImagePath(question.getOption2()));
	        	question.setOption3(fixImagePath(question.getOption3()));
	        	question.setOption4(fixImagePath(question.getOption4()));
	        }
	        
	        return questions != null ? questions : Collections.emptyList();
	    } catch (Exception e) {
	        System.err.println("공개 상태 질문 조회 실패: " + e.getMessage());
	        e.printStackTrace();
	        // 오류 발생 시 빈 리스트 반환 (null 대신)
	        return Collections.emptyList();
	    }
	}
}