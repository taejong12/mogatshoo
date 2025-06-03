package com.mogatshoo.dev.admin.amdinEmail.service;

import com.mogatshoo.dev.admin.amdinEmail.entity.AdminEmailEntity;
import com.mogatshoo.dev.voting_status.entity.StatusEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminEmailService {
    
    /**
     * 당첨자에게 축하 이메일 전송
     * 
     * @param serialNumber 질문 시리얼 번호
     * @param winnerId 당첨자 ID
     * @param senderId 전송자 ID (관리자)
     * @param customContent 추가 당첨 내용
     * @param attachmentFile 첨부할 사진 파일
     * @return 이메일 전송 결과 엔티티
     */
    AdminEmailEntity sendWinnerEmail(String serialNumber, String winnerId, String senderId, 
                                   String customContent, MultipartFile attachmentFile);
    
    /**
     * 투표 통계 정보를 기반으로 이메일 전송
     * 
     * @param statusEntity 투표 통계 정보
     * @param senderId 전송자 ID
     * @param customContent 추가 당첨 내용
     * @param attachmentFile 첨부할 사진 파일
     * @return 이메일 전송 결과
     */
    AdminEmailEntity sendWinnerEmailFromStatus(StatusEntity statusEntity, String senderId,
                                             String customContent, MultipartFile attachmentFile);
    
    /**
     * 이메일 전송 상태 업데이트
     * 
     * @param emailId 이메일 ID
     * @param status 새로운 상태
     * @param failureReason 실패 이유 (선택사항)
     * @return 업데이트된 엔티티
     */
    AdminEmailEntity updateEmailStatus(Long emailId, String status, String failureReason);
    
    /**
     * 특정 질문의 이메일 전송 이력 조회
     * 
     * @param serialNumber 질문 시리얼 번호
     * @return 이메일 전송 이력 목록
     */
    List<AdminEmailEntity> getEmailHistoryByQuestion(String serialNumber);
    
    /**
     * 특정 당첨자의 이메일 수신 이력 조회
     * 
     * @param winnerId 당첨자 ID
     * @return 이메일 수신 이력 목록
     */
    List<AdminEmailEntity> getEmailHistoryByWinner(String winnerId);
    
    /**
     * 관리자의 이메일 전송 이력 조회
     * 
     * @param senderId 전송자 ID
     * @return 이메일 전송 이력 목록
     */
    List<AdminEmailEntity> getEmailHistoryBySender(String senderId);
    
    /**
     * 이메일 중복 전송 여부 확인
     * 
     * @param serialNumber 질문 시리얼 번호
     * @param winnerId 당첨자 ID
     * @return 중복 전송 여부
     */
    boolean isDuplicateEmail(String serialNumber, String winnerId);
    
    /**
     * 전송 실패한 이메일 재전송
     * 
     * @param emailId 재전송할 이메일 ID
     * @param senderId 재전송자 ID
     * @return 재전송 결과
     */
    AdminEmailEntity resendFailedEmail(Long emailId, String senderId);
    
    /**
     * 이메일 전송 통계 조회
     * 
     * @return 전송 통계 정보
     */
    EmailStatistics getEmailStatistics();
    
    /**
     * 첨부파일 저장
     * 
     * @param file 첨부할 파일
     * @return 저장된 파일 경로
     */
    String saveAttachment(MultipartFile file);
    
    /**
     * 이메일 전송 통계 내부 클래스
     */
    public static class EmailStatistics {
        private Long totalSent;
        private Long totalPending;
        private Long totalFailed;
        
        public EmailStatistics(Long totalSent, Long totalPending, Long totalFailed) {
            this.totalSent = totalSent;
            this.totalPending = totalPending;
            this.totalFailed = totalFailed;
        }
        
        // Getters
        public Long getTotalSent() { return totalSent; }
        public Long getTotalPending() { return totalPending; }
        public Long getTotalFailed() { return totalFailed; }
        public Long getTotal() { return totalSent + totalPending + totalFailed; }
    }
}