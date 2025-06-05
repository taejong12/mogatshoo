package com.mogatshoo.dev.admin.question.service;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.repository.PictureRepository;
import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.repository.CompletedQuestionRepository;
import com.mogatshoo.dev.admin.question.repository.QuestionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private CompletedQuestionRepository completedQuestionRepository;
	
	@Autowired
	private PictureRepository picRepository;
	
	@Override
	public QuestionEntity createQuestion(QuestionEntity questionEntity) {
		logger.info("질문 생성 시작");
		
		try {
			// 질문 생성 전 다음 일련번호 설정
			String nextSerialNumber = generateNextSerialNumber();
			questionEntity.setSerialNumber(nextSerialNumber);
			
			// 기본값 설정
			questionEntity.setIsPublic("no");
			
			QuestionEntity savedQuestion = questionRepository.save(questionEntity);
			logger.info("질문 생성 완료: serialNumber={}, question={}", 
			           nextSerialNumber, questionEntity.getQuestion());
			
			return savedQuestion;
			
		} catch (Exception e) {
			logger.error("질문 생성 실패: error={}", e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public String generateNextSerialNumber() {
		logger.debug("다음 일련번호 생성 시작");
		
		try {
			String maxSerialNumber = questionRepository.findMaxSerialNumber();
			
			if (maxSerialNumber == null) {
				logger.info("첫 번째 질문 생성: 시작 번호 Q0001");
				return "Q0001";
			} else {
				// 기존 일련번호에서 숫자 부분 추출
				String numericPart = maxSerialNumber.substring(1);
				int numericValue = Integer.parseInt(numericPart);
				
				// +1 증가
				numericValue++;
				
				// 동일한 형식으로 포맷팅
				String nextSerial = String.format("Q%04d", numericValue);
				logger.debug("다음 일련번호 생성 완료: 현재 최대={}, 다음={}", maxSerialNumber, nextSerial);
				
				return nextSerial;
			}
		} catch (Exception e) {
			logger.error("일련번호 생성 실패: error={}", e.getMessage(), e);
			throw new RuntimeException("일련번호 생성 중 오류가 발생했습니다", e);
		}
	}
	
	@Override
	public List<QuestionEntity> getAllQuestions() {
		logger.debug("모든 질문 조회");
		
		try {
			List<QuestionEntity> questions = questionRepository.findAll();
			logger.info("모든 질문 조회 완료: count={}", questions.size());
			return questions;
			
		} catch (Exception e) {
			logger.error("모든 질문 조회 실패: error={}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<QuestionEntity> getAllQuestionsWithFixedImagePaths() {
		logger.info("이미지 경로 수정된 모든 질문 조회 시작");
		
		try {
			// 모든 질문을 가져온 후 이미지 경로가 포함된 경우 수정
			List<QuestionEntity> questions = questionRepository.findAll();
			
			// 각 질문의 옵션에서 잘못된 이미지 경로 수정
			for (QuestionEntity question : questions) {
				question.setOption1(fixImagePath(question.getOption1()));
				question.setOption2(fixImagePath(question.getOption2()));
				question.setOption3(fixImagePath(question.getOption3()));
				question.setOption4(fixImagePath(question.getOption4()));
			}
			
			logger.info("이미지 경로 수정 완료: count={}", questions.size());
			return questions;
			
		} catch (Exception e) {
			logger.error("이미지 경로 수정된 질문 조회 실패: error={}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
	
	// 이미지 경로 수정 헬퍼 메서드
	private String fixImagePath(String optionText) {
		if (optionText != null && optionText.contains("/uploads/")) {
			// HairLossTestService의 saveImageFile 메서드와 일치하도록 경로 검증
			if (!optionText.startsWith("/uploads/")) {
				// /uploads/로 시작하지 않는 경우 추가
				if (optionText.startsWith("uploads/")) {
					optionText = "/" + optionText;
					logger.debug("이미지 경로 수정: {}", optionText);
				}
			}
		}
		return optionText;
	}
	
	@Override
	public QuestionEntity getQuestionBySerialNumber(String serialNumber) {
		logger.info("질문 조회 시작: serialNumber={}", serialNumber);
		
		try {
			// 입력값 검증
			if (serialNumber == null || serialNumber.trim().isEmpty()) {
				logger.warn("유효하지 않은 serialNumber: {}", serialNumber);
				throw new IllegalArgumentException("일련번호가 없습니다");
			}
			
			logger.debug("조회할 serialNumber: '{}', 길이: {}", serialNumber, serialNumber.length());
			
			// 디버그용 전체 질문 조회 (개발 환경에서만)
			if (logger.isDebugEnabled()) {
				List<QuestionEntity> allQuestions = questionRepository.findAll();
				logger.debug("데이터베이스에 있는 모든 질문 일련번호 수: {}", allQuestions.size());
				for (QuestionEntity q : allQuestions) {
					logger.debug("DB 일련번호: '{}'", q.getSerialNumber());
				}
			}
			
			QuestionEntity question = questionRepository.findById(serialNumber)
				.orElseThrow(() -> {
					logger.error("질문을 찾을 수 없습니다: serialNumber={}", serialNumber);
					return new RuntimeException("Question not found with serial number: " + serialNumber);
				});
			
			logger.info("질문 조회 성공: serialNumber={}, question={}", 
			           question.getSerialNumber(), question.getQuestion());
			logger.debug("질문 상세 정보: 공개상태={}, 옵션1={}, 옵션2={}", 
			            question.getIsPublic(), question.getOption1(), question.getOption2());
			
			// 이미지 경로 수정
			question.setOption1(fixImagePath(question.getOption1()));
			question.setOption2(fixImagePath(question.getOption2()));
			question.setOption3(fixImagePath(question.getOption3()));
			question.setOption4(fixImagePath(question.getOption4()));
			
			logger.debug("이미지 경로 수정 완료: serialNumber={}", serialNumber);
			return question;
			
		} catch (Exception e) {
			logger.error("질문 조회 실패: serialNumber={}, error={}", serialNumber, e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public QuestionEntity updatePublicStatus(String serialNumber, String isPublic) {
		logger.info("공개 상태 업데이트 시작: serialNumber={}, isPublic={}", serialNumber, isPublic);
		
		try {
			QuestionEntity question = getQuestionBySerialNumber(serialNumber);
			String oldStatus = question.getIsPublic();
			question.setIsPublic(isPublic);
			
			QuestionEntity updatedQuestion = questionRepository.save(question);
			logger.info("공개 상태 업데이트 완료: serialNumber={}, {} → {}", 
			           serialNumber, oldStatus, isPublic);
			
			return updatedQuestion;
			
		} catch (Exception e) {
			logger.error("공개 상태 업데이트 실패: serialNumber={}, isPublic={}, error={}", 
			            serialNumber, isPublic, e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
		logger.info("질문 업데이트 시작: serialNumber={}", questionEntity.getSerialNumber());
		
		try {
		    // 기존 질문 조회
		    QuestionEntity existingQuestion = getQuestionBySerialNumber(questionEntity.getSerialNumber());
		    
		    // 질문과 보기 업데이트
		    existingQuestion.setQuestion(questionEntity.getQuestion());
		    existingQuestion.setOption1(questionEntity.getOption1());
		    existingQuestion.setOption2(questionEntity.getOption2());
		    existingQuestion.setOption3(questionEntity.getOption3());
		    existingQuestion.setOption4(questionEntity.getOption4());
		    
		    // isPublic 상태도 업데이트
		    existingQuestion.setIsPublic(questionEntity.getIsPublic());
		    
		    // 투표 기간 업데이트
		    existingQuestion.setVotingStartDate(questionEntity.getVotingStartDate());
		    existingQuestion.setVotingEndDate(questionEntity.getVotingEndDate());
		    existingQuestion.setVotingStatus(questionEntity.getVotingStatus());
		    
		    // 저장 및 반환
		    QuestionEntity updatedQuestion = questionRepository.save(existingQuestion);
		    logger.info("질문 업데이트 완료: serialNumber={}", questionEntity.getSerialNumber());
		    
		    return updatedQuestion;
		    
		} catch (Exception e) {
			logger.error("질문 업데이트 실패: serialNumber={}, error={}", 
			            questionEntity.getSerialNumber(), e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public void deleteQuestion(String serialNumber) {
		logger.info("질문 삭제 시작: serialNumber={}", serialNumber);
		
		try {
			// 질문 존재 여부 확인
			QuestionEntity question = getQuestionBySerialNumber(serialNumber);
			
			// 존재하면 삭제
			if (question != null) {
				questionRepository.delete(question);
				logger.info("질문 삭제 완료: serialNumber={}", serialNumber);
			} else {
				logger.error("삭제할 질문을 찾을 수 없습니다: serialNumber={}", serialNumber);
				throw new RuntimeException("삭제할 질문을 찾을 수 없습니다: " + serialNumber);
			}
			
		} catch (Exception e) {
			logger.error("질문 삭제 실패: serialNumber={}, error={}", serialNumber, e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public List<QuestionEntity> getQuestionsByPublicStatus(String isPublic) {
		logger.info("공개 상태별 질문 조회 시작: isPublic={}", isPublic);
		
	    try {
	        // QuestionRepository의 findByIsPublic 메소드를 호출하여 공개 상태에 따른 질문 목록 반환
	        List<QuestionEntity> questions = questionRepository.findByIsPublic(isPublic);
	        
	        if (questions == null) {
	            logger.warn("공개 상태별 질문 조회 결과가 null: isPublic={}", isPublic);
	            return Collections.emptyList();
	        }
	        
	        // 이미지 경로 수정
	        for (QuestionEntity question : questions) {
	        	question.setOption1(fixImagePath(question.getOption1()));
	        	question.setOption2(fixImagePath(question.getOption2()));
	        	question.setOption3(fixImagePath(question.getOption3()));
	        	question.setOption4(fixImagePath(question.getOption4()));
	        }
	        
	        logger.info("공개 상태별 질문 조회 완료: isPublic={}, count={}", isPublic, questions.size());
	        return questions;
	        
	    } catch (Exception e) {
	        logger.error("공개 상태별 질문 조회 실패: isPublic={}, error={}", isPublic, e.getMessage(), e);
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
		logger.info("만료된 투표 상태 업데이트 시작");
		
	    try {
	        // 모든 공개 질문들을 조회하여 상태 확인
	        List<QuestionEntity> publicQuestions = questionRepository.findByIsPublic("yes");
	        
	        logger.info("공개 질문 수: {}", publicQuestions.size());
	        
	        int updateCount = 0;
	        for (QuestionEntity question : publicQuestions) {
	            String currentStatus = question.getCurrentVotingStatus();
	            String oldStatus = question.getVotingStatus();
	            
	            // DB의 상태와 실제 상태가 다르면 업데이트
	            if (!currentStatus.equals(oldStatus)) {
	                question.setVotingStatus(currentStatus);
	                questionRepository.save(question);
	                updateCount++;
	                logger.info("질문 상태 변경: serialNumber={}, {} → {}", 
	                           question.getSerialNumber(), oldStatus, currentStatus);
	            }
	        }
	        
	        logger.info("만료된 투표 상태 업데이트 완료: 전체 질문 수={}, 업데이트 수={}", 
	                   publicQuestions.size(), updateCount);
	                   
	    } catch (Exception e) {
	        logger.error("투표 상태 업데이트 중 오류 발생: error={}", e.getMessage(), e);
	        throw new RuntimeException("투표 상태 업데이트 실패", e);
	    }
	}
	
	/**
	 * 종료된 지 하루가 지난 질문들을 완료 테이블로 이동
	 */
    @Override
    @Transactional
    public void archiveCompletedQuestions() {
        logger.info("완료된 질문 아카이빙 시작");
        
        try {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<QuestionEntity> questionsToArchive = questionRepository.findQuestionsForArchiving(oneDayAgo);
            
            logger.info("아카이빙 대상 질문 수: {}", questionsToArchive.size());
            
            int archivedCount = 0;
            for (QuestionEntity question : questionsToArchive) {
                try {
                    // 완료 테이블에 저장
                    CompletedQuestionEntity completedQuestion = new CompletedQuestionEntity(question);
                    completedQuestionRepository.save(completedQuestion);
                    
                    // 기존 테이블에서 삭제
                    questionRepository.delete(question);
                    
                    archivedCount++;
                    logger.debug("질문 아카이빙 완료: serialNumber={}", question.getSerialNumber());
                    
                } catch (Exception e) {
                    logger.error("개별 질문 아카이빙 실패: serialNumber={}, error={}", 
                                question.getSerialNumber(), e.getMessage(), e);
                    // 개별 실패는 전체 프로세스를 중단하지 않고 계속 진행
                }
            }
            
            logger.info("질문 아카이빙 완료: 대상 수={}, 성공 수={}", 
                       questionsToArchive.size(), archivedCount);
                       
        } catch (Exception e) {
            logger.error("질문 아카이빙 중 오류 발생: error={}", e.getMessage(), e);
            throw new RuntimeException("질문 아카이빙 실패", e);
        }
    }
    
    /**
	 * 페이징된 질문 목록 조회 (최신순 정렬)
	 */
	@Override
	public Page<QuestionEntity> getQuestionsWithPaging(Pageable pageable) {
		logger.info("페이징된 질문 목록 조회: 페이지={}, 크기={}", 
		           pageable.getPageNumber(), pageable.getPageSize());
		
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
			
			logger.info("페이징 조회 완료: 페이지={}/{}, 전체 요소={}, 현재 페이지 요소={}", 
			           pageable.getPageNumber(), questionPage.getTotalPages(), 
			           questionPage.getTotalElements(), questionPage.getNumberOfElements());
			
			return questionPage;
			
		} catch (Exception e) {
			logger.error("페이징 질문 조회 실패: 페이지={}, 크기={}, error={}", 
			            pageable.getPageNumber(), pageable.getPageSize(), e.getMessage(), e);
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
	
	/**
	 * 통합 검색 기능
	 */
	@Override
	public Page<QuestionEntity> searchQuestions(String keyword, String publicStatus, 
	                                           LocalDate createdDate, LocalDate votingDate, 
	                                           Pageable pageable) {
		logger.info("질문 검색 시작: keyword={}, publicStatus={}, createdDate={}, votingDate={}", 
		           keyword, publicStatus, createdDate, votingDate);
		
		try {
			// 검색 쿼리 실행
			Page<QuestionEntity> searchResult = questionRepository.searchQuestions(
			    keyword, publicStatus, createdDate, votingDate, pageable);
			
			// 각 질문의 이미지 경로 수정
			searchResult.getContent().forEach(question -> {
				question.setOption1(fixImagePath(question.getOption1()));
				question.setOption2(fixImagePath(question.getOption2()));
				question.setOption3(fixImagePath(question.getOption3()));
				question.setOption4(fixImagePath(question.getOption4()));
			});
			
			logger.info("질문 검색 완료: 검색 결과={}, 페이지={}/{}", 
			           searchResult.getTotalElements(), 
			           pageable.getPageNumber(), searchResult.getTotalPages());
			
			return searchResult;
			
		} catch (Exception e) {
			logger.error("질문 검색 실패: keyword={}, publicStatus={}, error={}", 
			            keyword, publicStatus, e.getMessage(), e);
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
}