package com.mogatshoo.dev.voting.repository;

import com.mogatshoo.dev.voting.entity.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingRepository extends JpaRepository<VotingEntity, Long> {
    
    /**
     * 특정 회원이 투표한 모든 질문의 일련번호 목록 조회
     */
    @Query("SELECT v.serialNumber FROM voting v WHERE v.voterId = :memberId")
    List<String> findSerialNumbersVotedByMember(@Param("memberId") String memberId);
    
    /**
     * 특정 회원의 모든 투표 이력 조회 (생성일 기준 내림차순)
     */
    List<VotingEntity> findByVoterIdOrderByCreatedAtDesc(String voterId);
    
    /**
     * 특정 회원이 특정 질문에 이미 투표했는지 확인
     */
    boolean existsBySerialNumberAndVoterId(String serialNumber, String voterId);
    
    /**
     * 특정 질문에 대한 모든 투표 조회
     */
    List<VotingEntity> findBySerialNumber(String serialNumber);
    
    /**
     * 특정 질문에서 각 회원별 투표 수 집계
     */
    @Query("SELECT v.votedId, COUNT(v) FROM voting v WHERE v.serialNumber = :serialNumber GROUP BY v.votedId")
    List<Object[]> countVotesByQuestionAndVotedId(@Param("serialNumber") String serialNumber);
}