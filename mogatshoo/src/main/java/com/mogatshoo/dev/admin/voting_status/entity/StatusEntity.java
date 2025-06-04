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
    
    // 고유 투표자 수 (실제 투표 참여한 회원 수) - 새로 추가
    private Long uniqueVoters;
    
    // 전체 멤버 수
    private Long totalMembers;
    
    // 투표율 (%) - 총 투표수 기준
    private Double votingRate;
    
    // 참여율 (%) - 고유 투표자 기준 - 새로 추가
    private Double participationRate;
    
    // 질문 생성일
    private LocalDateTime createdAt;
    
    // 각 옵션별 투표 결과 (옵션ID : 투표수)
    private Map<String, Long> voteDetails;
    
    // 이메일 전송 가능 여부 (참여율 1/3 이상)
    private Boolean emailEligible;
    
    /**
     * 투표율과 참여율 계산 및 이메일 전송 가능 여부 설정
     */
    public void calculateRates() {
        if (totalMembers != null && totalMembers > 0) {
            // 투표율: 총 투표수 / 전체 회원수 (중복 투표 포함)
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
            
            // 이메일 전송 가능 여부: 투표가 종료되고 투표율이 40% 이상인 경우
            this.emailEligible = "yes".equals(this.isEnded) && this.votingRate >= 40.0;
        } else {
            this.votingRate = 0.0;
            this.participationRate = 0.0;
            this.emailEligible = false;
        }
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
     * 종료 여부를 한글로 반환
     */
    public String getEndedStatusKorean() {
        return "no".equals(isEnded) ? "진행중" : "종료";
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
     * 투표 상태 요약 정보
     */
    public String getVotingSummary() {
        return String.format("참여율: %s, 총 투표: %d표, 참여자: %d명", 
                getFormattedParticipationRate(), 
                totalVotes != null ? totalVotes : 0,
                uniqueVoters != null ? uniqueVoters : 0);
    }
    
    /**
     * 투표 통계 상세 정보
     */
    public String getDetailedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("시리얼: ").append(serialNumber);
        sb.append(", 상태: ").append(getEndedStatusKorean());
        sb.append(", 투표율: ").append(getFormattedVotingRate());
        sb.append(", 참여율: ").append(getFormattedParticipationRate());
        sb.append(", 총투표: ").append(totalVotes != null ? totalVotes : 0);
        sb.append(", 참여자: ").append(uniqueVoters != null ? uniqueVoters : 0);
        sb.append(", 최다득표: ").append(topVotedName != null ? topVotedName : "없음");
        sb.append("(").append(topVoteCount != null ? topVoteCount : 0).append("표)");
        return sb.toString();
    }
    
    /**
     * 이메일 전송 가능 여부를 문자열로 반환
     */
    public String getEmailEligibleStatus() {
        return emailEligible != null && emailEligible ? "전송 가능" : "전송 불가";
    }
}