package com.mogatshoo.dev.voting_status.service;

import com.mogatshoo.dev.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.voting_status.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusRepository statusRepository;

    @Override
    public List<StatusEntity> getAllVotingStatistics() {
        try {
            List<StatusEntity> statistics = new ArrayList<>();
            
            // 전체 멤버 수 조회
            Long totalMembers = statusRepository.getTotalMemberCount();
            
            if (totalMembers == 0) {
                System.out.println("등록된 회원이 없습니다.");
            }
            
            // 모든 질문 정보 조회
            List<Map<String, Object>> questionInfos = statusRepository.getAllQuestionInfo();
            
            if (questionInfos.isEmpty()) {
                System.out.println("등록된 질문이 없습니다.");
                return statistics; // 빈 리스트 반환
            }
            
            for (Map<String, Object> questionInfo : questionInfos) {
                String serialNumber = (String) questionInfo.get("serialNumber");
                
                // 각 질문별 통계 생성
                StatusEntity statusEntity = createStatusEntity(questionInfo, totalMembers);
                
                if (statusEntity != null) {
                    statistics.add(statusEntity);
                }
            }
            
            System.out.println("총 " + statistics.size() + "개 질문의 투표 통계를 조회했습니다.");
            return statistics;
            
        } catch (Exception e) {
            System.err.println("전체 투표 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public StatusEntity getQuestionStatistics(String serialNumber) {
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                return null;
            }
            
            // 질문 상세 정보 조회
            Map<String, Object> questionDetail = statusRepository.getQuestionDetail(serialNumber);
            if (questionDetail == null) {
                System.out.println("질문 " + serialNumber + "를 찾을 수 없습니다.");
                return null;
            }
            
            // 전체 멤버 수 조회
            Long totalMembers = statusRepository.getTotalMemberCount();
            
            // 통계 엔티티 생성
            StatusEntity statusEntity = createStatusEntity(questionDetail, totalMembers);
            
            System.out.println("질문 " + serialNumber + "의 통계를 조회했습니다: " + statusEntity);
            return statusEntity;
            
        } catch (Exception e) {
            System.err.println("질문별 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void refreshStatistics() {
        try {
            // 통계 캐시 갱신 로직 (필요시 구현)
            System.out.println("투표 통계 데이터를 새로고침했습니다.");
        } catch (Exception e) {
            System.err.println("통계 새로고침 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<StatusEntity> getEmailEligibleQuestions() {
        try {
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            return allStatistics.stream()
                    .filter(stat -> stat.getEmailEligible() != null && stat.getEmailEligible())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("이메일 전송 가능 질문 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Double calculateVotingRate(Long totalVotes, Long totalMembers) {
        if (totalMembers == null || totalMembers == 0) {
            return 0.0;
        }
        
        if (totalVotes == null) {
            totalVotes = 0L;
        }
        
        return (double) totalVotes / totalMembers * 100;
    }

    /**
     * 질문 정보를 바탕으로 StatusEntity 생성
     */
    private StatusEntity createStatusEntity(Map<String, Object> questionInfo, Long totalMembers) {
        try {
            String serialNumber = (String) questionInfo.get("serialNumber");
            
            // 투표 통계 조회
            Map<String, Object> voteStats = statusRepository.getQuestionVoteStatistics(serialNumber);
            
            // StatusEntity 생성
            StatusEntity statusEntity = new StatusEntity();
            statusEntity.setSerialNumber(serialNumber);
            statusEntity.setQuestionContent((String) questionInfo.get("questionContent"));
            statusEntity.setIsEnded((String) questionInfo.get("isEnded"));
            statusEntity.setCreatedAt((LocalDateTime) questionInfo.get("createdAt"));
            
            // 투표 통계 설정
            statusEntity.setTopVotedId((String) voteStats.get("topVotedId"));
            statusEntity.setTopVotedName((String) voteStats.get("topVotedName"));
            statusEntity.setTopVoteCount((Long) voteStats.get("topVoteCount"));
            statusEntity.setTotalVotes((Long) voteStats.get("totalVotes"));
            statusEntity.setVoteDetails((Map<String, Long>) voteStats.get("voteDetails"));
            
            // 멤버 정보 및 투표율 계산
            statusEntity.setTotalMembers(totalMembers);
            statusEntity.calculateVotingRate();
            
            return statusEntity;
            
        } catch (Exception e) {
            System.err.println("StatusEntity 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}