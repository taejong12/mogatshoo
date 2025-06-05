package com.mogatshoo.dev.admin.question.scheduler;

import com.mogatshoo.dev.admin.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 투표 상태 및 질문 아카이빙 스케줄러
 * - 투표 상태 업데이트 (보류 → 진행중 → 종료)
 * - 종료된 질문을 completed_question으로 이동
 */
@Component
@EnableScheduling
public class VotingStatusScheduler {
    
    @Autowired
    private QuestionService questionService;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 매 10분마다 투표 상태 업데이트
     * - 시작 시간이 된 질문: 보류 → 진행중
     * - 종료 시간이 된 질문: 진행중 → 종료
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void updateVotingStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("=== 투표 상태 업데이트 시작 ===");
            System.out.println("실행 시간: " + now.format(formatter));
            
            questionService.updateExpiredVotingStatus();
            
            System.out.println("=== 투표 상태 업데이트 완료 ===");
        } catch (Exception e) {
            System.err.println("투표 상태 업데이트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 매 시간마다 완료된 질문 아카이빙
     * - 종료된 지 1시간이 지난 질문을 completed_question으로 이동
     * - 기존 question 테이블에서 삭제
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    public void archiveCompletedQuestions() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("=== 질문 아카이빙 시작 ===");
            System.out.println("실행 시간: " + now.format(formatter));
            
            questionService.archiveCompletedQuestions();
            
            System.out.println("=== 질문 아카이빙 완료 ===");
        } catch (Exception e) {
            System.err.println("질문 아카이빙 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 매일 자정에 전체 데이터 정리 및 상태 동기화
     * - 모든 질문의 상태를 실제 시간과 동기화
     * - 누락된 아카이빙 작업 보완
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 (00:00:00)
    public void dailyDataCleanup() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("=== 일일 데이터 정리 시작 ===");
            System.out.println("실행 시간: " + now.format(formatter));
            
            // 1. 투표 상태 동기화
            questionService.updateExpiredVotingStatus();
            
            // 2. 누락된 아카이빙 작업 수행
            questionService.archiveCompletedQuestions();
            
            System.out.println("=== 일일 데이터 정리 완료 ===");
        } catch (Exception e) {
            System.err.println("일일 데이터 정리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 수동 실행용 메서드 (관리자가 필요시 호출)
     */
    public void manualArchive() {
        try {
            System.out.println("=== 수동 아카이빙 실행 ===");
            questionService.updateExpiredVotingStatus();
            questionService.archiveCompletedQuestions();
            System.out.println("=== 수동 아카이빙 완료 ===");
        } catch (Exception e) {
            System.err.println("수동 아카이빙 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}