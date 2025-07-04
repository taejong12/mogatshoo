package com.mogatshoo.dev.admin.voting_status.service;

import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StatusService {
    
    /**
     * 모든 질문의 투표 통계 조회 (기존 메서드 - 호환성 유지)
     * @return 질문별 투표 통계 리스트
     */
    List<StatusEntity> getAllVotingStatistics();
    
    /**
     * 모든 질문의 투표 통계 조회 (페이지네이션)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징된 질문별 투표 통계
     */
    Page<StatusEntity> getAllVotingStatistics(Pageable pageable);
    
    /**
     * 특정 질문의 투표 통계 조회
     * @param serialNumber 질문 시리얼 넘버
     * @return 해당 질문의 투표 통계
     */
    StatusEntity getQuestionStatistics(String serialNumber);
    
    /**
     * 통계 데이터 새로고침
     * 캐시된 데이터가 있다면 갱신
     */
    void refreshStatistics();
    
    /**
     * 이메일 전송 가능한 질문 목록 조회
     * (투표율 1/3 이상인 질문들)
     * @return 이메일 전송 가능한 질문 통계 리스트
     */
    List<StatusEntity> getEmailEligibleQuestions();
    
    /**
     * 투표율 계산
     * @param totalVotes 총 투표수
     * @param totalMembers 전체 회원수
     * @return 투표율 (%)
     */
    Double calculateVotingRate(Long totalVotes, Long totalMembers);
    
    Page<StatusEntity> searchVotingStatistics(String keyword, String publicStatus, String completionStatus, Pageable pageable);
}