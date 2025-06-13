package com.mogatshoo.dev.admin.voting_status.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatusEntity {
    
    // 질문 시리얼 넘버
    private String serialNumber;
    
    // 질문 내용
    private String questionContent;
    
    // 질문 종료 여부 (공개 상태: yes/no)
    private String isEnded;
    
    // 최다득표자 ID
    private String topVotedId;
    
    // 최다득표자 이름
    private String topVotedName;
    
    // 최다득표수
    private Long topVoteCount;
    
    // 총 투표수 (전체 투표 행위 횟수)
    private Long totalVotes;
    
    // 고유 투표자 수 (실제 투표 참여한 회원 수)
    private Long uniqueVoters;
    
    // 전체 멤버 수
    private Long totalMembers;
    
    // 투표율 (%) - 총 투표수 기준
    private Double votingRate;
    
    // 참여율 (%) - 고유 투표자 기준
    private Double participationRate;
    
    // 투표 참여자 기준 투표율 (%) - 새로 추가
    private Double voterBasedVotingRate;
    
    // **신규 추가: 최다득표자 득표율 (%)**
    private Double topVotedRate;
    
    // 질문 생성일
    private LocalDateTime createdAt;
    
    // 투표 기간 필드 추가
    private LocalDateTime votingStartDate;
    private LocalDateTime votingEndDate;
    
    // 각 옵션별 투표 결과 (옵션ID : 투표수)
    private Map<String, Long> voteDetails;
    
    // 테이블 소스 (active/completed)
    private String tableSource;
    
    // 완료 여부
    private Boolean isCompleted;
    
    // 이메일 전송 가능 여부
    private Boolean emailEligible;
    
    /**
     * 현재 시간을 기준으로 실제 투표 상태를 동적으로 계산
     * @return 투표 상태 ("보류", "진행중", "종료")
     */
    public String getCurrentVotingStatus() {
        // 완료된 질문은 항상 종료 상태
        if ("completed".equals(this.tableSource)) {
            return "종료";
        }
        
        // 비공개 상태라면 상태 관계없이 보류
        if (!"yes".equals(this.isEnded)) {
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
     * 투표율, 참여율, 득표율 계산 및 이메일 전송 가능 여부 설정
     */
    public void calculateRates() {
        if (totalMembers != null && totalMembers > 0) {
            // 기존 투표율: 총 투표수 / 전체 회원수 (중복 투표 포함)
            if (totalVotes != null) {
                this.votingRate = (double) totalVotes / totalMembers * 100;
            } else {
                this.votingRate = 0.0;
            }

            // 참여율: 고유 투표자 수 / 전체 회원수 (실제 참여한 사람 기준)
            if (uniqueVoters != null) {
                this.participationRate = (double) uniqueVoters / totalMembers * 100;
            } else {
                this.participationRate = 0.0;
            }
        } else {
            this.votingRate = 0.0;
            this.participationRate = 0.0;
        }
        
        // 투표 참여자 기준 투표율 계산
        if (uniqueVoters != null && uniqueVoters > 0) {
            // 투표 참여자 기준 투표율: 총 투표수 / 실제 투표 참여자 수
            if (totalVotes != null) {
                this.voterBasedVotingRate = (double) totalVotes / uniqueVoters * 100;
            } else {
                this.voterBasedVotingRate = 0.0;
            }
        } else {
            this.voterBasedVotingRate = 0.0;
        }
        
        // **신규 추가: 최다득표자 득표율 계산**
        if (totalVotes != null && totalVotes > 0 && topVoteCount != null) {
            this.topVotedRate = (double) topVoteCount / totalVotes * 100;
        } else {
            this.topVotedRate = 0.0;
        }
        
        // **새로운 이메일 전송 가능 여부 조건:**
        // 1. 투표가 종료되고
        // 2. 전체 회원 대비 참여율이 50% 이상이고
        // 3. 최다득표자의 득표율이 40% 이상인 경우
        this.emailEligible = "종료".equals(getCurrentVotingStatus()) 
                            && this.participationRate >= 30.0 
                            && this.topVotedRate >= 40.0;
    }
    
    /**
     * 기존 메서드 호환성을 위해 유지
     * @deprecated calculateRates() 사용 권장
     */
    @Deprecated
    public void calculateVotingRate() {
        calculateRates();
    }
    
    /**
     * 종료 여부를 한글로 반환 (기존 호환성)
     */
    public String getEndedStatusKorean() {
        return getCurrentVotingStatus();
    }
    
    /**
     * 투표율을 소수점 2자리로 포맷
     */
    public String getFormattedVotingRate() {
        return String.format("%.2f%%", votingRate != null ? votingRate : 0.0);
    }
    
    /**
     * 참여율을 소수점 2자리로 포맷
     */
    public String getFormattedParticipationRate() {
        return String.format("%.2f%%", participationRate != null ? participationRate : 0.0);
    }
    
    /**
     * 투표 참여자 기준 투표율을 소수점 2자리로 포맷
     */
    public String getFormattedVoterBasedVotingRate() {
        return String.format("%.2f%%", voterBasedVotingRate != null ? voterBasedVotingRate : 0.0);
    }
    
    /**
     * **신규 추가: 최다득표자 득표율을 소수점 2자리로 포맷**
     */
    public String getFormattedTopVotedRate() {
        return String.format("%.2f%%", topVotedRate != null ? topVotedRate : 0.0);
    }
    
    /**
     * 투표 효율성 계산 (고유 투표자당 평균 투표수)
     */
    public Double getAverageVotesPerVoter() {
        if (uniqueVoters != null && uniqueVoters > 0 && totalVotes != null) {
            return (double) totalVotes / uniqueVoters;
        }
        return 0.0;
    }
    
    /**
     * 투표 효율성을 소수점 2자리로 포맷
     */
    public String getFormattedAverageVotesPerVoter() {
        return String.format("%.2f", getAverageVotesPerVoter());
    }
    
    /**
     * **업데이트된 투표 상태 요약 정보**
     */
    public String getVotingSummary() {
        return String.format("참여율: %s, 최다득표율: %s, 총 투표: %d표, 참여자: %d명", 
                getFormattedParticipationRate(),
                getFormattedTopVotedRate(),
                totalVotes != null ? totalVotes : 0,
                uniqueVoters != null ? uniqueVoters : 0);
    }
    
    /**
     * **업데이트된 투표 통계 상세 정보**
     */
    public String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("시리얼: ").append(serialNumber);
        sb.append(", 상태: ").append(getCurrentVotingStatus());
        sb.append(", 투표율: ").append(getFormattedVotingRate());
        sb.append(", 참여율: ").append(getFormattedParticipationRate());
        sb.append(", 최다득표율: ").append(getFormattedTopVotedRate());
        sb.append(", 참여자기준투표율: ").append(getFormattedVoterBasedVotingRate());
        sb.append(", 총투표: ").append(totalVotes != null ? totalVotes : 0);
        sb.append(", 참여자: ").append(uniqueVoters != null ? uniqueVoters : 0);
        sb.append(", 최다득표: ").append(topVotedName != null ? topVotedName : "없음");
        sb.append("(").append(topVoteCount != null ? topVoteCount : 0).append("표)");
        return sb.toString();
    }
    
    /**
     * **업데이트된 이메일 전송 가능 여부를 상세 문자열로 반환**
     */
    public String getEmailEligibleStatus() {
        if (!"종료".equals(getCurrentVotingStatus())) {
            return "전송 불가 (투표 미종료)";
        }
        
        if (participationRate == null || participationRate < 30.0) {
            return String.format("전송 불가 (참여율 부족: %.1f%% < 50%%)", 
                                participationRate != null ? participationRate : 0.0);
        }
        
        if (topVotedRate == null || topVotedRate < 40.0) {
            return String.format("전송 불가 (득표율 부족: %.1f%% < 40%%)", 
                                topVotedRate != null ? topVotedRate : 0.0);
        }
        
        return String.format("전송 가능 (참여율: %.1f%%, 득표율: %.1f%%)", 
                           participationRate, topVotedRate);
    }
    
    /**
     * **새로운 이메일 전송 조건 검사 메서드**
     */
    public boolean isEmailEligible() {
        return emailEligible != null && emailEligible;
    }
    
    /**
     * **이메일 전송 조건별 상세 체크**
     */
    public Map<String, Boolean> getEmailEligibilityDetails() {
        Map<String, Boolean> details = new java.util.HashMap<>();
        details.put("isVotingEnded", "종료".equals(getCurrentVotingStatus()));
        details.put("hasEnoughParticipation", participationRate != null && participationRate >= 30.0);
        details.put("hasEnoughTopVotedRate", topVotedRate != null && topVotedRate >= 40.0);
        details.put("isOverallEligible", isEmailEligible());
        return details;
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