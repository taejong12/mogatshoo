package com.mogatshoo.dev.admin.question.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="question")
@Table(name="question")
@Setter
@Getter
@ToString
public class QuestionEntity {
    
    @Id
    private String serialNumber;
    
    @Column(nullable = false)
    private String question;
    
    @Column(nullable = false)
    private String option1;
    
    @Column(nullable = false)
    private String option2;
    
    @Column(nullable = false)
    private String option3;
    
    @Column(nullable = false)
    private String option4;
    
    @Column(name = "option1_member_id")
    private String option1MemberId;

    @Column(name = "option2_member_id")  
    private String option2MemberId;

    @Column(name = "option3_member_id")
    private String option3MemberId;

    @Column(name = "option4_member_id")
    private String option4MemberId;
    
    @Column(nullable = false, columnDefinition = "VARCHAR(3) DEFAULT 'no'")
    private String isPublic = "no";
    
    // 투표 기간 필드
    @Column(name = "voting_start_date")
    private LocalDateTime votingStartDate;
    
    @Column(name = "voting_end_date")
    private LocalDateTime votingEndDate;
    
    // 투표 상태 필드 (DB 저장용, 실제로는 동적 계산 사용)
    @Column(name = "voting_status", columnDefinition = "VARCHAR(10) DEFAULT '진행중'")
    private String votingStatus = "진행중";
    
    @Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;
    
    /**
     * 현재 시간을 기준으로 실제 투표 상태를 동적으로 계산
     * @return 투표 상태 ("보류", "진행중", "종료")
     */
    public String getCurrentVotingStatus() {
        // 비공개 상태라면 상태 관계없이 보류
        if (!"yes".equals(this.isPublic)) {
            return "보류";
        }
        
        // 투표 시간이 설정되지 않았다면 보류
        if (this.votingStartDate == null || this.votingEndDate == null) {
            return "보류";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 투표 시작 시간 전이면 보류
        if (now.isBefore(this.votingStartDate)) {
            return "보류";
        }
        
        // 투표 종료 시간 후면 종료
        if (now.isAfter(this.votingEndDate)) {
            return "종료";
        }
        
        // 투표 기간 중이면 진행중
        return "진행중";
    }
    
    /**
     * 투표 상태를 한글로 반환 (호환성을 위한 메서드)
     */
    public String getVotingStatusKorean() {
        return getCurrentVotingStatus();
    }
    
    /**
     * 투표 가능 여부 확인
     * @return 투표 가능하면 true, 불가능하면 false
     */
    public boolean isVotingAvailable() {
        return "진행중".equals(getCurrentVotingStatus());
    }
    
    /**
     * 투표 상태 요약 정보
     * @return 상태와 시간 정보를 포함한 문자열
     */
    public String getVotingStatusSummary() {
        String status = getCurrentVotingStatus();
        
        if (votingStartDate == null || votingEndDate == null) {
            return status + " (투표 시간 미설정)";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case "보류":
                if (now.isBefore(votingStartDate)) {
                    return status + " (시작 예정: " + votingStartDate.toString().replace("T", " ") + ")";
                }
                return status;
            case "진행중":
                return status + " (종료 예정: " + votingEndDate.toString().replace("T", " ") + ")";
            case "종료":
                return status + " (종료: " + votingEndDate.toString().replace("T", " ") + ")";
            default:
                return status;
        }
    }
    
    /**
     * 투표 기간 정보를 문자열로 반환
     */
    public String getVotingPeriodInfo() {
        if (votingStartDate == null || votingEndDate == null) {
            return "투표 기간 미설정";
        }
        
        return String.format("%s ~ %s", 
            votingStartDate.toString().replace("T", " "), 
            votingEndDate.toString().replace("T", " "));
    }
}