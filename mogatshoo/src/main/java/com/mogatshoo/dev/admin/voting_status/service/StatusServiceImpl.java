package com.mogatshoo.dev.admin.voting_status.service;

import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.repository.StatusRepository;
import com.mogatshoo.dev.admin.question.service.QuestionService; // 추가
import com.mogatshoo.dev.admin.question.entity.QuestionEntity; // 추가

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private QuestionService questionService;
    
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
    public Page<StatusEntity> getAllVotingStatistics(Pageable pageable) {
        try {
            // 전체 데이터 조회
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            // 페이지네이션 적용
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allStatistics.size());
            
            List<StatusEntity> pagedStatistics;
            if (start > allStatistics.size()) {
                pagedStatistics = new ArrayList<>();
            } else {
                pagedStatistics = allStatistics.subList(start, end);
            }
            
            return new PageImpl<>(pagedStatistics, pageable, allStatistics.size());
            
        } catch (Exception e) {
            System.err.println("페이징된 투표 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
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
            String tableSource = (String) questionInfo.get("tableSource");
            
            // 투표 통계 조회
            Map<String, Object> voteStats = statusRepository.getQuestionVoteStatistics(serialNumber);
            
            // StatusEntity 생성
            StatusEntity statusEntity = new StatusEntity();
            statusEntity.setSerialNumber(serialNumber);
            statusEntity.setQuestionContent((String) questionInfo.get("questionContent"));
            statusEntity.setIsEnded((String) questionInfo.get("isEnded"));
            statusEntity.setTableSource(tableSource);
            statusEntity.setIsCompleted("completed".equals(tableSource));
            
            // 투표 기간 정보 설정 - 직접 DB에서 가져온 데이터 사용
            Object votingStartDateObj = questionInfo.get("votingStartDate");
            Object votingEndDateObj = questionInfo.get("votingEndDate");
            
            System.out.println("=== 투표 기간 정보 디버깅 ===");
            System.out.println("질문: " + serialNumber);
            System.out.println("시작일 객체: " + votingStartDateObj + " (타입: " + (votingStartDateObj != null ? votingStartDateObj.getClass().getName() : "null") + ")");
            System.out.println("종료일 객체: " + votingEndDateObj + " (타입: " + (votingEndDateObj != null ? votingEndDateObj.getClass().getName() : "null") + ")");
            
            if (votingStartDateObj instanceof java.sql.Timestamp) {
                statusEntity.setVotingStartDate(((java.sql.Timestamp) votingStartDateObj).toLocalDateTime());
                System.out.println("시작일 설정됨: " + statusEntity.getVotingStartDate());
            } else if (votingStartDateObj instanceof LocalDateTime) {
                statusEntity.setVotingStartDate((LocalDateTime) votingStartDateObj);
                System.out.println("시작일 설정됨 (LocalDateTime): " + statusEntity.getVotingStartDate());
            } else {
                System.out.println("시작일 null로 설정");
                statusEntity.setVotingStartDate(null);
            }
            
            if (votingEndDateObj instanceof java.sql.Timestamp) {
                statusEntity.setVotingEndDate(((java.sql.Timestamp) votingEndDateObj).toLocalDateTime());
                System.out.println("종료일 설정됨: " + statusEntity.getVotingEndDate());
            } else if (votingEndDateObj instanceof LocalDateTime) {
                statusEntity.setVotingEndDate((LocalDateTime) votingEndDateObj);
                System.out.println("종료일 설정됨 (LocalDateTime): " + statusEntity.getVotingEndDate());
            } else {
                System.out.println("종료일 null로 설정");
                statusEntity.setVotingEndDate(null);
            }
            
            // 만약 DB에서 가져온 데이터가 null이면 QuestionService에서 다시 시도
            if (statusEntity.getVotingStartDate() == null || statusEntity.getVotingEndDate() == null) {
                System.out.println("DB 데이터가 null이므로 QuestionService에서 재시도");
                try {
                    if ("active".equals(tableSource)) {
                        QuestionEntity questionEntity = questionService.getQuestionBySerialNumber(serialNumber);
                        if (questionEntity != null) {
                            if (statusEntity.getVotingStartDate() == null) {
                                statusEntity.setVotingStartDate(questionEntity.getVotingStartDate());
                                System.out.println("QuestionService에서 시작일 설정: " + statusEntity.getVotingStartDate());
                            }
                            if (statusEntity.getVotingEndDate() == null) {
                                statusEntity.setVotingEndDate(questionEntity.getVotingEndDate());
                                System.out.println("QuestionService에서 종료일 설정: " + statusEntity.getVotingEndDate());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("QuestionService에서 투표 기간 정보 조회 실패: " + e.getMessage());
                }
            }
            
            // createdAt 타입 변환 처리
            Object createdAtObj = questionInfo.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                statusEntity.setCreatedAt(((java.sql.Timestamp) createdAtObj).toLocalDateTime());
            } else if (createdAtObj instanceof LocalDateTime) {
                statusEntity.setCreatedAt((LocalDateTime) createdAtObj);
            } else {
                statusEntity.setCreatedAt(null);
            }
            
            // 투표 통계 설정
            statusEntity.setTopVotedId((String) voteStats.get("topVotedId"));
            statusEntity.setTopVotedName((String) voteStats.get("topVotedName"));
            statusEntity.setTopVoteCount((Long) voteStats.get("topVoteCount"));
            statusEntity.setTotalVotes((Long) voteStats.get("totalVotes"));
            statusEntity.setUniqueVoters((Long) voteStats.get("uniqueVoters"));
            statusEntity.setVoteDetails((Map<String, Long>) voteStats.get("voteDetails"));
            
            // 멤버 정보 및 투표율 계산
            statusEntity.setTotalMembers(totalMembers);
            statusEntity.calculateRates();
            
            System.out.println("=== 최종 StatusEntity 투표 기간 ===");
            System.out.println("시작일: " + statusEntity.getVotingStartDate());
            System.out.println("종료일: " + statusEntity.getVotingEndDate());
            System.out.println("현재 상태: " + statusEntity.getCurrentVotingStatus());
            
            return statusEntity;
            
        } catch (Exception e) {
            System.err.println("StatusEntity 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Page<StatusEntity> searchVotingStatistics(String keyword, String publicStatus, 
                                                    String completionStatus, Pageable pageable) {
        try {
            List<StatusEntity> statistics = new ArrayList<>();
            Long totalMembers = statusRepository.getTotalMemberCount();
            
            // 검색된 질문 정보 조회
            List<Map<String, Object>> questionInfos = statusRepository.searchQuestionInfo(keyword, publicStatus, completionStatus);
            
            for (Map<String, Object> questionInfo : questionInfos) {
                StatusEntity statusEntity = createStatusEntity(questionInfo, totalMembers);
                if (statusEntity != null) {
                    statistics.add(statusEntity);
                }
            }
            
            // 페이지네이션 적용
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), statistics.size());
            
            List<StatusEntity> pagedStatistics;
            if (start > statistics.size()) {
                pagedStatistics = new ArrayList<>();
            } else {
                pagedStatistics = statistics.subList(start, end);
            }
            
            return new PageImpl<>(pagedStatistics, pageable, statistics.size());
            
        } catch (Exception e) {
            System.err.println("투표 통계 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
}