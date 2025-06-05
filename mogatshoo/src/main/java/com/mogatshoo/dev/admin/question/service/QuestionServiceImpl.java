package com.mogatshoo.dev.admin.question.service;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.repository.PictureRepository;
import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.repository.CompletedQuestionRepository;
import com.mogatshoo.dev.admin.question.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private CompletedQuestionRepository completedQuestionRepository; // 추가된 Repository
	
	@Autowired
	private PictureRepository picRepository;
	
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
	    
	    // 투표 기간 업데이트 추가
	    existingQuestion.setVotingStartDate(questionEntity.getVotingStartDate());
	    existingQuestion.setVotingEndDate(questionEntity.getVotingEndDate());
	    existingQuestion.setVotingStatus(questionEntity.getVotingStatus());
	    
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
	
    /**
     * 투표 기간이 만료된 질문들의 상태를 '종료'로 업데이트
     */
	@Override
	@Transactional
	public void updateExpiredVotingStatus() {
	    try {
	        // 모든 공개 질문들을 조회하여 상태 확인
	        List<QuestionEntity> publicQuestions = questionRepository.findByIsPublic("yes");
	        
	        System.out.println("공개 질문 수: " + publicQuestions.size());
	        
	        for (QuestionEntity question : publicQuestions) {
	            String currentStatus = question.getCurrentVotingStatus();
	            
	            // DB의 상태와 실제 상태가 다르면 업데이트
	            if (!currentStatus.equals(question.getVotingStatus())) {
	                question.setVotingStatus(currentStatus);
	                questionRepository.save(question);
	                System.out.println("질문 " + question.getSerialNumber() + 
	                    " 상태 변경: " + question.getVotingStatus() + " → " + currentStatus);
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("투표 상태 업데이트 중 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 종료된 지 하루가 지난 질문들을 완료 테이블로 이동
	 */
    @Override
    @Transactional
    public void archiveCompletedQuestions() {
        try {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<QuestionEntity> questionsToArchive = questionRepository.findQuestionsForArchiving(oneDayAgo);
            
            System.out.println("아카이빙 대상 질문 수: " + questionsToArchive.size());
            
            for (QuestionEntity question : questionsToArchive) {
                // 완료 테이블에 저장
                CompletedQuestionEntity completedQuestion = new CompletedQuestionEntity(question);
                completedQuestionRepository.save(completedQuestion);
                
                // 기존 테이블에서 삭제
                questionRepository.delete(question);
                
                System.out.println("질문 " + question.getSerialNumber() + " 아카이빙 완료");
            }
        } catch (Exception e) {
            System.err.println("질문 아카이빙 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
	 * 페이징된 질문 목록 조회 (최신순 정렬)
	 */
	@Override
	public Page<QuestionEntity> getQuestionsWithPaging(Pageable pageable) {
		try {
			// 페이징된 질문 목록 조회
			Page<QuestionEntity> questionPage = questionRepository.findAll(pageable);
			
			// 각 질문의 이미지 경로 수정
			questionPage.getContent().forEach(question -> {
				question.setOption1(fixImagePath(question.getOption1()));
				question.setOption2(fixImagePath(question.getOption2()));
				question.setOption3(fixImagePath(question.getOption3()));
				question.setOption4(fixImagePath(question.getOption4()));
			});
			
			System.out.println("페이징 조회 결과:");
			System.out.println("- 페이지 번호: " + pageable.getPageNumber());
			System.out.println("- 페이지 크기: " + pageable.getPageSize());
			System.out.println("- 전체 요소 수: " + questionPage.getTotalElements());
			System.out.println("- 전체 페이지 수: " + questionPage.getTotalPages());
			System.out.println("- 현재 페이지 요소 수: " + questionPage.getNumberOfElements());
			
			return questionPage;
		} catch (Exception e) {
			System.err.println("페이징 질문 조회 실패: " + e.getMessage());
			e.printStackTrace();
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
	
	/**
	 * 통합 검색 기능
	 */
	@Override
	public Page<QuestionEntity> searchQuestions(String keyword, String publicStatus, LocalDate createdDate, LocalDate votingDate, Pageable pageable) {
		try {
			System.out.println("=== 검색 실행 ===");
			System.out.println("키워드: " + keyword);
			System.out.println("공개상태: " + publicStatus);
			System.out.println("생성날짜: " + createdDate);
			System.out.println("투표날짜: " + votingDate);
			
			// 검색 쿼리 실행
			Page<QuestionEntity> searchResult = questionRepository.searchQuestions(keyword, publicStatus, createdDate, votingDate, pageable);
			
			// 각 질문의 이미지 경로 수정
			searchResult.getContent().forEach(question -> {
				question.setOption1(fixImagePath(question.getOption1()));
				question.setOption2(fixImagePath(question.getOption2()));
				question.setOption3(fixImagePath(question.getOption3()));
				question.setOption4(fixImagePath(question.getOption4()));
			});
			
			System.out.println("검색 결과: " + searchResult.getTotalElements() + "개");
			
			return searchResult;
		} catch (Exception e) {
			System.err.println("검색 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
    
}