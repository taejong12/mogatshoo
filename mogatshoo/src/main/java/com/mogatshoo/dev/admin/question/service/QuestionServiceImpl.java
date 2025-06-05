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
import java.util.ArrayList;
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
		logger.info("새 질문 생성 시작");
		
		try {
			// 질문 생성 전 다음 일련번호 설정
			String nextSerialNumber = generateNextSerialNumber();
			questionEntity.setSerialNumber(nextSerialNumber);
			
			// 기본값 설정
			questionEntity.setIsPublic("no");
			
			QuestionEntity savedQuestion = questionRepository.save(questionEntity);
			logger.info("질문 생성 완료 - 일련번호: {}, 질문: {}", 
					   nextSerialNumber, questionEntity.getQuestion());
			
			return savedQuestion;
		} catch (Exception e) {
			logger.error("질문 생성 중 오류 발생", e);
			throw e;
		}
	}
	
	@Override
	public String generateNextSerialNumber() {
		logger.debug("다음 일련번호 생성 시작");
		
		try {
			String maxSerialNumber = questionRepository.findMaxSerialNumber();
			
			if (maxSerialNumber == null) {
				logger.info("첫 번째 질문 일련번호 생성: Q0001");
				return "Q0001";
			} else {
				// 기존 일련번호에서 숫자 부분 추출
				String numericPart = maxSerialNumber.substring(1);
				int numericValue = Integer.parseInt(numericPart);
				
				// +1 증가
				numericValue++;
				
				// 동일한 형식으로 포맷팅
				String nextSerialNumber = String.format("Q%04d", numericValue);
				logger.debug("다음 일련번호 생성 완료 - 이전: {}, 다음: {}", maxSerialNumber, nextSerialNumber);
				
				return nextSerialNumber;
			}
		} catch (Exception e) {
			logger.error("일련번호 생성 중 오류 발생", e);
			throw new RuntimeException("일련번호 생성 실패", e);
		}
	}
	
	@Override
	public List<QuestionEntity> getAllQuestions() {
		logger.info("전체 질문 목록 조회 시작");
		
		try {
			List<QuestionEntity> questions = questionRepository.findAll();
			logger.info("전체 질문 목록 조회 완료 - {}개", questions.size());
			return questions;
		} catch (Exception e) {
			logger.error("전체 질문 목록 조회 중 오류 발생", e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<QuestionEntity> getAllQuestionsWithFixedImagePaths() {
		logger.info("이미지 경로 수정된 전체 질문 목록 조회 시작");
		
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
			
			logger.info("이미지 경로 수정된 질문 목록 조회 완료 - {}개", questions.size());
			return questions;
		} catch (Exception e) {
			logger.error("이미지 경로 수정된 질문 목록 조회 중 오류 발생", e);
			return Collections.emptyList();
		}
	}
	
	// 이미지 경로 수정 헬퍼 메서드
	private String fixImagePath(String optionText) {
		if (optionText != null && optionText.contains("/uploads/")) {
			logger.debug("이미지 경로 수정 처리: {}", optionText);
			
			// HairLossTestService의 saveImageFile 메서드와 일치하도록 경로 검증
			if (!optionText.startsWith("/uploads/")) {
				// /uploads/로 시작하지 않는 경우 추가
				if (optionText.startsWith("uploads/")) {
					optionText = "/" + optionText;
					logger.debug("이미지 경로 수정됨: {}", optionText);
				}
			}
		}
		return optionText;
	}
	
	@Override
	public QuestionEntity getQuestionBySerialNumber(String serialNumber) {
		logger.info("질문 조회 시작 - 일련번호: {}", serialNumber);
		
		try {
			if (serialNumber == null || serialNumber.trim().isEmpty()) {
				logger.warn("질문 일련번호가 null이거나 빈 문자열입니다.");
				throw new IllegalArgumentException("질문 일련번호가 필요합니다.");
			}
			
			logger.debug("조회할 serialNumber 정보 - 값: '{}', 길이: {}", 
						serialNumber, serialNumber.length());
			
			QuestionEntity question = questionRepository.findById(serialNumber)
				.orElseThrow(() -> {
					logger.error("질문을 찾을 수 없습니다 - 일련번호: '{}'", serialNumber);
					return new RuntimeException("Question not found with serial number: " + serialNumber);
				});
			
			logger.info("질문 조회 성공 - 일련번호: {}, 질문: {}", 
					   question.getSerialNumber(), question.getQuestion());
			
			// 이미지 경로 수정
			question.setOption1(fixImagePath(question.getOption1()));
			question.setOption2(fixImagePath(question.getOption2()));
			question.setOption3(fixImagePath(question.getOption3()));
			question.setOption4(fixImagePath(question.getOption4()));
			
			logger.debug("이미지 경로 수정 완료");
			return question;
		} catch (Exception e) {
			logger.error("질문 조회 중 오류 발생 - 일련번호: {}", serialNumber, e);
			throw e;
		}
	}
	
	@Override
	public QuestionEntity updatePublicStatus(String serialNumber, String isPublic) {
		logger.info("질문 공개상태 업데이트 시작 - 일련번호: {}, 공개상태: {}", serialNumber, isPublic);
		
		try {
			QuestionEntity question = getQuestionBySerialNumber(serialNumber);
			String oldStatus = question.getIsPublic();
			
			question.setIsPublic(isPublic);
			QuestionEntity updatedQuestion = questionRepository.save(question);
			
			logger.info("질문 공개상태 업데이트 완료 - 일련번호: {}, 변경: {} → {}", 
					   serialNumber, oldStatus, isPublic);
			
			return updatedQuestion;
		} catch (Exception e) {
			logger.error("질문 공개상태 업데이트 중 오류 발생 - 일련번호: {}", serialNumber, e);
			throw e;
		}
	}

	@Override
	public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
		logger.info("질문 업데이트 시작 - 일련번호: {}", questionEntity.getSerialNumber());
		
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
			
			logger.info("질문 업데이트 완료 - 일련번호: {}, 제목: {}", 
					   updatedQuestion.getSerialNumber(), updatedQuestion.getQuestion());
			
			return updatedQuestion;
		} catch (Exception e) {
			logger.error("질문 업데이트 중 오류 발생 - 일련번호: {}", questionEntity.getSerialNumber(), e);
			throw e;
		}
	}
	
	@Override
	public void deleteQuestion(String serialNumber) {
		logger.info("질문 삭제 시작 - 일련번호: {}", serialNumber);
		
		try {
			// 질문 존재 여부 확인
			QuestionEntity question = getQuestionBySerialNumber(serialNumber);
			
			// 존재하면 삭제
			if (question != null) {
				questionRepository.delete(question);
				logger.info("질문 삭제 완료 - 일련번호: {}", serialNumber);
			} else {
				logger.error("삭제할 질문을 찾을 수 없습니다 - 일련번호: {}", serialNumber);
				throw new RuntimeException("삭제할 질문을 찾을 수 없습니다: " + serialNumber);
			}
		} catch (Exception e) {
			logger.error("질문 삭제 중 오류 발생 - 일련번호: {}", serialNumber, e);
			throw e;
		}
	}
	
	@Override
	public List<QuestionEntity> getQuestionsByPublicStatus(String isPublic) {
		logger.info("공개상태별 질문 목록 조회 시작 - 공개상태: {}", isPublic);
		
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
			
			logger.info("공개상태별 질문 목록 조회 완료 - 공개상태: {}, 개수: {}", 
					   isPublic, questions != null ? questions.size() : 0);
			
			return questions != null ? questions : Collections.emptyList();
		} catch (Exception e) {
			logger.error("공개상태별 질문 조회 중 오류 발생 - 공개상태: {}", isPublic, e);
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
		logger.info("투표 상태 업데이트 시작");
		
		try {
			LocalDateTime now = LocalDateTime.now();
			logger.debug("현재 시간: {}", now);
			
			// 모든 공개 질문들을 조회하여 상태 확인
			List<QuestionEntity> publicQuestions = questionRepository.findByIsPublic("yes");
			logger.info("공개 질문 수: {}", publicQuestions.size());
			
			int updatedCount = 0;
			for (QuestionEntity question : publicQuestions) {
				String oldStatus = question.getVotingStatus();
				String newStatus = question.getCurrentVotingStatus();
				
				// DB의 상태와 실제 상태가 다르면 업데이트
				if (!newStatus.equals(oldStatus)) {
					question.setVotingStatus(newStatus);
					questionRepository.save(question);
					updatedCount++;
					
					logger.info("질문 상태 변경 - 일련번호: {}, 변경: {} → {}, 시작: {}, 종료: {}", 
							   question.getSerialNumber(), oldStatus, newStatus, 
							   question.getVotingStartDate(), question.getVotingEndDate());
				}
			}
			
			logger.info("투표 상태 업데이트 완료 - {}개 질문 상태 변경", updatedCount);
			
		} catch (Exception e) {
			logger.error("투표 상태 업데이트 중 오류 발생", e);
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
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime oneHourAgo = now.minusHours(1); // 1시간 전으로 변경
			
			logger.debug("아카이빙 기준 시간 - 현재: {}, 기준(1시간 전): {}", now, oneHourAgo);
			
			// 종료된 지 1시간이 지난 질문들 조회
			List<QuestionEntity> allQuestions = questionRepository.findAll();
			List<QuestionEntity> questionsToArchive = new ArrayList<>();
			
			for (QuestionEntity question : allQuestions) {
				// 동적 상태 확인
				String currentStatus = question.getCurrentVotingStatus();
				
				// 종료 상태이고, 종료 시간이 1시간 이전인 질문들
				if ("종료".equals(currentStatus) && 
					question.getVotingEndDate() != null && 
					question.getVotingEndDate().isBefore(oneHourAgo)) {
					
					questionsToArchive.add(question);
					logger.debug("아카이빙 대상 발견 - 일련번호: {}, 종료시간: {}", 
								question.getSerialNumber(), question.getVotingEndDate());
				}
			}
			
			logger.info("아카이빙 대상 질문 수: {}", questionsToArchive.size());
			
			int successCount = 0;
			int failCount = 0;
			
			for (QuestionEntity question : questionsToArchive) {
				try {
					// 1. 완료 테이블에 저장
					CompletedQuestionEntity completedQuestion = new CompletedQuestionEntity(question);
					completedQuestionRepository.save(completedQuestion);
					logger.debug("완료 테이블에 저장 완료 - 일련번호: {}", question.getSerialNumber());
					
					// 2. 기존 테이블에서 삭제
					questionRepository.delete(question);
					logger.info("질문 아카이빙 완료 - 일련번호: {}", question.getSerialNumber());
					
					successCount++;
				} catch (Exception e) {
					logger.error("질문 아카이빙 실패 - 일련번호: {}", question.getSerialNumber(), e);
					failCount++;
					// 개별 질문 아카이빙 실패해도 계속 진행
				}
			}
			
			if (questionsToArchive.size() > 0) {
				logger.info("아카이빙 완료 - 성공: {}개, 실패: {}개", successCount, failCount);
			} else {
				logger.info("아카이빙 대상 질문 없음");
			}
			
		} catch (Exception e) {
			logger.error("질문 아카이빙 중 오류 발생", e);
		}
	}
	
	/**
	 * 페이징된 질문 목록 조회 (최신순 정렬)
	 */
	@Override
	public Page<QuestionEntity> getQuestionsWithPaging(Pageable pageable) {
		logger.info("페이징된 질문 목록 조회 시작 - 페이지: {}, 크기: {}", 
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
			
			logger.info("페이징된 질문 목록 조회 완료 - 전체: {}개, 현재페이지: {}개, 전체페이지: {}", 
					   questionPage.getTotalElements(), questionPage.getNumberOfElements(), 
					   questionPage.getTotalPages());
			
			return questionPage;
		} catch (Exception e) {
			logger.error("페이징된 질문 목록 조회 중 오류 발생", e);
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
	
	/**
	 * 통합 검색 기능
	 */
	@Override
	public Page<QuestionEntity> searchQuestions(String keyword, String publicStatus, 
											   LocalDate createdDate, LocalDate votingDate, Pageable pageable) {
		logger.info("질문 검색 시작 - 키워드: {}, 공개상태: {}, 생성날짜: {}, 투표날짜: {}", 
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
			
			logger.info("질문 검색 완료 - 결과: {}개", searchResult.getTotalElements());
			
			return searchResult;
		} catch (Exception e) {
			logger.error("질문 검색 중 오류 발생 - 키워드: {}", keyword, e);
			// 오류 발생 시 빈 페이지 반환
			return Page.empty(pageable);
		}
	}
}