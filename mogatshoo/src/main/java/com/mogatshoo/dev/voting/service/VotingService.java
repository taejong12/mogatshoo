package com.mogatshoo.dev.voting.service;

import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.voting.entity.VotingEntity;

import java.util.List;
import java.util.Map;

public interface VotingService {
    
    /**
     * 사용자가 아직 투표하지 않은 공개 질문 중 랜덤으로 하나 가져오기
     * 
     * @param memberId 현재 사용자 ID
     * @return 랜덤 질문 또는 없으면 null
     */
    QuestionEntity getRandomUnansweredQuestion(String memberId);
    
    /**
     * 투표 저장
     * 
     * @param serialNumber 질문 일련번호
     * @param voterId 투표한 회원 ID
     * @param votedId 투표 받은 회원 ID
     * @return 저장된 투표 엔티티
     */
    VotingEntity saveVote(String serialNumber, String voterId, String votedId);
    
    /**
     * 특정 질문에 대한 회원별 투표 수 집계
     * 
     * @param serialNumber 질문 일련번호
     * @return 회원ID와 투표수 맵 (키: 회원ID, 값: 투표수)
     */
    Map<String, Long> getVoteCountsByQuestion(String serialNumber);
    
    /**
     * 회원의 투표 이력 조회
     * 
     * @param voterId 회원 ID
     * @return 투표 이력 목록
     */
    List<VotingEntity> getVoteHistoryByVoterId(String voterId);
    
    /**
     * 회원이 특정 질문에 이미 투표했는지 확인
     * 
     * @param serialNumber 질문 일련번호
     * @param voterId 회원 ID
     * @return 투표 여부
     */
    boolean hasVoted(String serialNumber, String voterId);
}