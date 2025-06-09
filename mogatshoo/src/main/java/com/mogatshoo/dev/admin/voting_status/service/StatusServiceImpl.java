package com.mogatshoo.dev.admin.voting_status.service;

import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.repository.StatusRepository;
import com.mogatshoo.dev.admin.question.service.QuestionService;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private QuestionService questionService;
    
    @Override
    public List<StatusEntity> getAllVotingStatistics() {
        logger.info("전체 투표 통계 조회 시작");
        
        try {
            List<StatusEntity> statistics = new ArrayList<>();
            
            // 전체 멤버 수 조회
            Long totalMembers = statusRepository.getTotalMemberCount();
            logger.debug("전체 멤버 수: {}", totalMembers);
            
            if (totalMembers == 0) {
                logger.warn("등록된 회원이 없습니다.");
            }
            
            // 모든 질문 정보 조회
            List<Map<String, Object>> questionInfos = statusRepository.getAllQuestionInfo();
            
            if (questionInfos.isEmpty()) {
                logger.warn("등록된 질문이 없습니다.");
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
            
            logger.info("총 {}개 질문의 투표 통계를 조회 완료", statistics.size());
            return statistics;
            
        } catch (Exception e) {
            logger.error("전체 투표 통계 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Page<StatusEntity> getAllVotingStatistics(Pageable pageable) {
        logger.info("페이징된 투표 통계 조회 시작 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            // 전체 데이터 조회
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            // 페이지네이션 적용
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allStatistics.size());
            
            List<StatusEntity> pagedStatistics;
            if (start > allStatistics.size()) {
                pagedStatistics = new ArrayList<>();
                logger.warn("페이지 시작 인덱스({})가 전체 데이터 크기({})를 초과", start, allStatistics.size());
            } else {
                pagedStatistics = allStatistics.subList(start, end);
            }
            
            logger.info("페이징 처리 완료 - 전체 {}개 중 {}개 반환", allStatistics.size(), pagedStatistics.size());
            return new PageImpl<>(pagedStatistics, pageable, allStatistics.size());
            
        } catch (Exception e) {
            logger.error("페이징된 투표 통계 조회 중 오류 발생", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public StatusEntity getQuestionStatistics(String serialNumber) {
        logger.info("질문별 통계 조회 시작 - 질문번호: {}", serialNumber);
        
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                logger.warn("질문번호가 null이거나 빈 문자열입니다.");
                return null;
            }
            
            // 질문 상세 정보 조회
            Map<String, Object> questionDetail = statusRepository.getQuestionDetail(serialNumber);
            if (questionDetail == null) {
                logger.warn("질문을 찾을 수 없습니다 - 질문번호: {}", serialNumber);
                return null;
            }
            
            // 전체 멤버 수 조회
            Long totalMembers = statusRepository.getTotalMemberCount();
            
            // 통계 엔티티 생성
            StatusEntity statusEntity = createStatusEntity(questionDetail, totalMembers);
            
            logger.info("질문별 통계 조회 완료 - 질문번호: {}, 전체투표수: {}, 최다득표율: {}%", 
                       serialNumber, 
                       statusEntity != null ? statusEntity.getTotalVotes() : 0,
                       statusEntity != null ? statusEntity.getFormattedTopVotedRate() : "0.00%");
            return statusEntity;
            
        } catch (Exception e) {
            logger.error("질문별 통계 조회 중 오류 발생 - 질문번호: {}", serialNumber, e);
            return null;
        }
    }

    @Override
    public void refreshStatistics() {
        logger.info("투표 통계 새로고침 시작");
        
        try {
            // 통계 캐시 갱신 로직 (필요시 구현)
            logger.info("투표 통계 데이터 새로고침 완료");
        } catch (Exception e) {
            logger.error("통계 새로고침 중 오류 발생", e);
        }
    }

    @Override
    public List<StatusEntity> getEmailEligibleQuestions() {
        logger.info("이메일 전송 가능 질문 조회 시작 (새로운 조건: 참여율 50% + 득표율 40%)");
        
        try {
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            // **업데이트된 필터링 조건**
            List<StatusEntity> eligibleQuestions = allStatistics.stream()
                    .filter(stat -> {
                        boolean isEnded = "종료".equals(stat.getCurrentVotingStatus());
                        boolean hasEnoughParticipation = stat.getParticipationRate() != null && stat.getParticipationRate() >= 50.0;
                        boolean hasEnoughTopVotedRate = stat.getTopVotedRate() != null && stat.getTopVotedRate() >= 40.0;
                        
                        logger.debug("질문 {}: 종료={}, 참여율={}%(>=50%), 득표율={}%(>=40%)", 
                                    stat.getSerialNumber(), isEnded, stat.getParticipationRate(), stat.getTopVotedRate());
                        
                        return isEnded && hasEnoughParticipation && hasEnoughTopVotedRate;
                    })
                    .collect(Collectors.toList());
                    
            logger.info("이메일 전송 가능 질문 {}개 조회 완료 (조건: 참여율≥50% AND 득표율≥40%)", eligibleQuestions.size());
            return eligibleQuestions;
                    
        } catch (Exception e) {
            logger.error("이메일 전송 가능 질문 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Double calculateVotingRate(Long totalVotes, Long totalMembers) {
        logger.debug("투표율 계산 - 투표수: {}, 전체멤버: {}", totalVotes, totalMembers);
        
        if (totalMembers == null || totalMembers == 0) {
            logger.warn("전체 멤버 수가 0이거나 null입니다.");
            return 0.0;
        }
        
        if (totalVotes == null) {
            totalVotes = 0L;
        }
        
        double rate = (double) totalVotes / totalMembers * 100;
        logger.debug("계산된 투표율: {}%", rate);
        return rate;
    }

    /**
     * **업데이트된 질문 정보를 바탕으로 StatusEntity 생성**
     * 최다득표율 계산 로직 추가
     */
    private StatusEntity createStatusEntity(Map<String, Object> questionInfo, Long totalMembers) {
        String serialNumber = (String) questionInfo.get("serialNumber");
        logger.debug("StatusEntity 생성 시작 - 질문번호: {}", serialNumber);
        
        try {
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
            
            logger.debug("투표 기간 정보 - 질문: {}, 시작일: {}, 종료일: {}", 
                        serialNumber, votingStartDateObj, votingEndDateObj);
            
            if (votingStartDateObj instanceof java.sql.Timestamp) {
                statusEntity.setVotingStartDate(((java.sql.Timestamp) votingStartDateObj).toLocalDateTime());
                logger.debug("시작일 설정 완료 (Timestamp): {}", statusEntity.getVotingStartDate());
            } else if (votingStartDateObj instanceof LocalDateTime) {
                statusEntity.setVotingStartDate((LocalDateTime) votingStartDateObj);
                logger.debug("시작일 설정 완료 (LocalDateTime): {}", statusEntity.getVotingStartDate());
            } else {
                logger.debug("시작일 null로 설정");
                statusEntity.setVotingStartDate(null);
            }
            
            if (votingEndDateObj instanceof java.sql.Timestamp) {
                statusEntity.setVotingEndDate(((java.sql.Timestamp) votingEndDateObj).toLocalDateTime());
                logger.debug("종료일 설정 완료 (Timestamp): {}", statusEntity.getVotingEndDate());
            } else if (votingEndDateObj instanceof LocalDateTime) {
                statusEntity.setVotingEndDate((LocalDateTime) votingEndDateObj);
                logger.debug("종료일 설정 완료 (LocalDateTime): {}", statusEntity.getVotingEndDate());
            } else {
                logger.debug("종료일 null로 설정");
                statusEntity.setVotingEndDate(null);
            }
            
            // 만약 DB에서 가져온 데이터가 null이면 QuestionService에서 다시 시도
            if (statusEntity.getVotingStartDate() == null || statusEntity.getVotingEndDate() == null) {
                logger.debug("DB 데이터가 null이므로 QuestionService에서 재시도 - 질문번호: {}", serialNumber);
                try {
                    if ("active".equals(tableSource)) {
                        QuestionEntity questionEntity = questionService.getQuestionBySerialNumber(serialNumber);
                        if (questionEntity != null) {
                            if (statusEntity.getVotingStartDate() == null) {
                                statusEntity.setVotingStartDate(questionEntity.getVotingStartDate());
                                logger.debug("QuestionService에서 시작일 설정: {}", statusEntity.getVotingStartDate());
                            }
                            if (statusEntity.getVotingEndDate() == null) {
                                statusEntity.setVotingEndDate(questionEntity.getVotingEndDate());
                                logger.debug("QuestionService에서 종료일 설정: {}", statusEntity.getVotingEndDate());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("QuestionService에서 투표 기간 정보 조회 실패 - 질문번호: {}", serialNumber, e);
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
            
            // 멤버 정보 및 모든 비율 계산 (최다득표율 포함)
            statusEntity.setTotalMembers(totalMembers);
            statusEntity.calculateRates(); // 이 메서드에서 topVotedRate도 계산됨
            
            logger.debug("StatusEntity 생성 완료 - 질문번호: {}, 투표상태: {}, 참여율: {}%, 득표율: {}%, 이메일발송가능: {}", 
                        serialNumber, 
                        statusEntity.getCurrentVotingStatus(),
                        statusEntity.getFormattedParticipationRate(),
                        statusEntity.getFormattedTopVotedRate(),
                        statusEntity.isEmailEligible());
            
            return statusEntity;
            
        } catch (Exception e) {
            logger.error("StatusEntity 생성 중 오류 발생 - 질문번호: {}", serialNumber, e);
            return null;
        }
    }
    
    @Override
    public Page<StatusEntity> searchVotingStatistics(String keyword, String publicStatus, 
                                                    String completionStatus, Pageable pageable) {
        logger.info("투표 통계 검색 시작 - 키워드: {}, 공개상태: {}, 완료상태: {}", 
                   keyword, publicStatus, completionStatus);
        
        try {
            List<StatusEntity> statistics = new ArrayList<>();
            Long totalMembers = statusRepository.getTotalMemberCount();
            
            // 검색된 질문 정보 조회
            List<Map<String, Object>> questionInfos = statusRepository.searchQuestionInfo(keyword, publicStatus, completionStatus);
            logger.debug("검색된 질문 수: {}", questionInfos.size());
            
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
                logger.warn("검색 결과 페이지 시작 인덱스({})가 전체 크기({})를 초과", start, statistics.size());
            } else {
                pagedStatistics = statistics.subList(start, end);
            }
            
            logger.info("투표 통계 검색 완료 - 전체 {}개 중 {}개 반환", statistics.size(), pagedStatistics.size());
            return new PageImpl<>(pagedStatistics, pageable, statistics.size());
            
        } catch (Exception e) {
            logger.error("투표 통계 검색 중 오류 발생 - 키워드: {}", keyword, e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
}