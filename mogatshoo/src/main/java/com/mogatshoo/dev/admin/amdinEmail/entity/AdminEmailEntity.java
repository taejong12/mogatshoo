package com.mogatshoo.dev.admin.amdinEmail.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="admin_email")
@Table(name="admin_email")
@Setter
@Getter
@ToString
public class AdminEmailEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serialNumber; // 질문 시리얼 번호
    
    @Column(nullable = false)
    private String winnerId; // 당첨자 ID
    
    @Column(nullable = false)
    private String winnerName; // 당첨자 이름
    
    @Column(nullable = false)
    private String winnerEmail; // 당첨자 이메일
    
    @Column(nullable = false)
    private String questionContent; // 질문 내용
    
    @Column(nullable = false)
    private Double votingRate; // 득표율
    
    @Column(nullable = false)
    private Long voteCount; // 득표수
    
    @Column(nullable = false)
    private String emailSubject; // 이메일 제목
    
    @Column(columnDefinition = "TEXT")
    private String emailContent; // 이메일 내용
    
    @Column(columnDefinition = "TEXT")
    private String customContent; // 사용자 정의 당첨 내용 (추가)
    
    @Column
    private String attachmentPath; // 첨부파일 경로 (사진)
    
    @Column
    private String attachmentOriginalName; // 첨부파일 원본 이름 (추가)
    
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String emailStatus = "PENDING"; // 이메일 전송 상태 (PENDING, SENT, FAILED)
    
    @Column
    private String failureReason; // 전송 실패 이유
    
    @Column(nullable = false)
    private String senderId; // 전송자 ID (관리자)
    
    @Column(columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime sentAt; // 전송 완료 시간
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * 전송 상태를 한글로 반환
     */
    public String getEmailStatusKorean() {
        switch (emailStatus) {
            case "SENT": return "전송완료";
            case "FAILED": return "전송실패";
            case "PENDING": return "전송대기";
            default: return "알수없음";
        }
    }
    
    /**
     * 득표율을 포맷된 문자열로 반환
     */
    public String getFormattedVotingRate() {
        return String.format("%.2f%%", votingRate != null ? votingRate : 0.0);
    }
}