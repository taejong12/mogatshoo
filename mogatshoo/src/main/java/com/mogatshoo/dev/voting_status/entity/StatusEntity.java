package com.mogatshoo.dev.voting_status.entity;

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
    
    //질문 시리얼 넘버
    private String serialNumber;
    //질문 내용
    private String questionContent;
    //질문 종료 여부 (공개 상태: yes/no)
    private String isEnded;
    //최다득표자 ID
    private String topVotedId;
    //최다득표자 이름
    private String topVotedName;
    //최다득표수
    private Long topVoteCount;
    //총 투표수
    private Long totalVotes;
	//전체 멤버 수
    private Long totalMembers;
    //투표율 (%)
    private Double votingRate;
    //질문 생성일
    private LocalDateTime createdAt;
    //각 옵션별 투표 결과 (옵션ID : 투표수)
    private Map<String, Long> voteDetails;
    //이메일 전송 가능 여부 (투표율 1/3 이상)
    private Boolean emailEligible;
    
    //투표율 계산 및 이메일 전송 가능 여부 설정
    public void calculateVotingRate() {
        if (totalMembers != null && totalMembers > 0) {
            this.votingRate = (double) totalVotes / totalMembers * 100;
            this.emailEligible = this.votingRate >= 33.33;
        } else {
            this.votingRate = 0.0;
            this.emailEligible = false;
        }
    }
    
    //종료 여부를 한글로 반환
    public String getEndedStatusKorean() {
        return "no".equals(isEnded) ? "진행중" : "종료";
    }
    
	//투표율을 소수점 2자리로 포맷
    public String getFormattedVotingRate() {
        return String.format("%.2f%%", votingRate != null ? votingRate : 0.0);
    }
}
