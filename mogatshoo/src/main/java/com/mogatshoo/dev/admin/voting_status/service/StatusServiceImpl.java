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
                logger.warn("등록된 회원이 없습니다");
                return statistics;
            }
            
            // 모든 질문 정보 조회
            List<Map<String, Object>> questionInfos = statusRepository.getAllQuestionInfo();
            logger.debug("조회된 질문 수: {}", questionInfos.size());
            
            if (questionInfos.isEmpty()) {
                logger.warn("등록된 질문이 없습니다");
                return statistics;
            }
            
            int successCount = 0;
            for (Map<String, Object> questionInfo : questionInfos) {
                String serialNumber = (String) questionInfo.get("serialNumber");
                
                try {
                    // 각 질문별 통계 생성
                    StatusEntity statusEntity = createStatusEntity(questionInfo, totalMembers);
                    
                    if (statusEntity != null) {
                        statistics.add(statusEntity);
                        successCount++;
                        logger.debug("질문 통계 생성 성공: serialNumber={}", serialNumber);
                    } else {
                        logger.warn("질문 통계 생성 실패: serialNumber={}", serialNumber);
                    }
                } catch (Exception e) {
                    logger.error("개별 질문 통계 생성 실패: serialNumber={}, error={}", 
                                serialNumber, e.getMessage(), e);
                    // 개별 실패는 전체 프로세스를 중단하지 않고 계속 진행
                }
            }
            
            logger.info("전체 투표 통계 조회 완료: 전체 질문={}, 성공={}", 
                       questionInfos.size(), successCount);
            return statistics;
            
        } catch (Exception e) {
            logger.error("전체 투표 통계 조회 실패: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Page<StatusEntity> getAllVotingStatistics(Pageable pageable) {
        logger.info("페이징된 투표 통계 조회: 페이지={}, 크기={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            // 전체 데이터 조회
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            // 페이지네이션 적용
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allStatistics.size());
            
            List<StatusEntity> pagedStatistics;
            if (start > allStatistics.size()) {
                logger.warn("요청한 페이지가 범위를 벗어남: start={}, totalSize={}", 
                           start, allStatistics.size());
                pagedStatistics = new ArrayList<>();
            } else {
                pagedStatistics = allStatistics.subList(start, end);
                logger.debug("페이징 적용: start={}, end={}, 실제크기={}", start, end, pagedStatistics.size());
            }
            
            Page<StatusEntity> result = new PageImpl<>(pagedStatistics, pageable, allStatistics.size());
            logger.info("페이징된 투표 통계 조회 완료: 페이지={}/{}, 현재페이지요소={}, 전체요소={}", 
                       pageable.getPageNumber(), result.getTotalPages(), 
                       result.getNumberOfElements(), result.getTotalElements());
            
            return result;
            
        } catch (Exception e) {
            logger.error("페이징된 투표 통계 조회 실패: 페이지={}, 크기={}, error={}", 
                        pageable.getPageNumber(), pageable.getPageSize(), e.getMessage(), e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public StatusEntity getQuestionStatistics(String serialNumber) {
        logger.info("질문별 통계 조회 시작: serialNumber={}", serialNumber);
        
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                logger.warn("유효하지 않은 serialNumber: {}", serialNumber);
                return null;
            }
            
            // 질문 상세 정보 조회
            Map<String, Object> questionDetail = statusRepository.getQuestionDetail(serialNumber);
            if (questionDetail == null) {
                logger.warn("질문을 찾을 수 없습니다: serialNumber={}", serialNumber);
                return null;
            }
            
            // 전체 멤버 수 조회
            Long totalMembers = statusRepository.getTotalMemberCount();
            logger.debug("전체 멤버 수: {}", totalMembers);
            
            // 통계 엔티티 생성
            StatusEntity statusEntity = createStatusEntity(questionDetail, totalMembers);
            
            if (statusEntity != null) {
                logger.info("질문별 통계 조회 성공: serialNumber={}, 총투표={}, 투표율={}%", 
                           serialNumber, statusEntity.getTotalVotes(), statusEntity.getVotingRate());
            } else {
                logger.error("통계 엔티티 생성 실패: serialNumber={}", serialNumber);
            }
            
            return statusEntity;
            
        } catch (Exception e) {
            logger.error("질문별 통계 조회 실패: serialNumber={}, error={}", 
                        serialNumber, e.getMessage(), e);
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
            logger.error("통계 새로고침 실패: error={}", e.getMessage(), e);
            throw new RuntimeException("통계 새로고침 중 오류가 발생했습니다", e);
        }
    }

    @Override
    public List<StatusEntity> getEmailEligibleQuestions() {
        logger.info("이메일 전송 가능 질문 조회 시작");
        
        try {
            List<StatusEntity> allStatistics = getAllVotingStatistics();
            
            List<StatusEntity> eligibleQuestions = allStatistics.stream()
                    .filter(stat -> stat.getEmailEligible() != null && stat.getEmailEligible())
                    .collect(Collectors.toList());
            
            logger.info("이메일 전송 가능 질문 조회 완료: 전체 질문={}, 전송 가능={}", 
                       allStatistics.size(), eligibleQuestions.size());
            
            return eligibleQuestions;
                    
        } catch (Exception e) {
            logger.error("이메일 전송 가능 질문 조회 실패: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Double calculateVotingRate(Long totalVotes, Long totalMembers) {
        logger.debug("투표율 계산: totalVotes={}, totalMembers={}", totalVotes, totalMembers);
        
        try {
            if (totalMembers == null || totalMembers == 0) {
                logger.warn("전체 멤버 수가 0이거나 null: totalMembers={}", totalMembers);
                return 0.0;
            }
            
            if (totalVotes == null) {
                logger.debug("총 투표 수가 null, 0으로 처리");
                totalVotes = 0L;
            }
            
            double votingRate = (double) totalVotes / totalMembers * 100;
            logger.debug("투표율 계산 완료: {}%", String.format("%.2f", votingRate));
            
            return votingRate;
            
        } catch (Exception e) {
            logger.error("투표율 계산 실패: totalVotes={}, totalMembers={}, error={}", 
                        totalVotes, totalMembers, e.getMessage(), e);
            return 0.0;
        }
    }

    /**
     * 질문 정보를 바탕으로 StatusEntity 생성
     */
    private StatusEntity createStatusEntity(Map<String, Object> questionInfo, Long totalMembers) {
        String serialNumber = (String) questionInfo.get("serialNumber");
        logger.debug("StatusEntity 생성 시작: serialNumber={}", serialNumber);
        
        try {
            // 투표 통계 조회
            Map<String, Object> voteStats = statusRepository.getQuestionVoteStatistics(serialNumber);
            
            // StatusEntity 생성
            StatusEntity statusEntity = new StatusEntity();
            statusEntity.setSerialNumber(serialNumber);
            statusEntity.setQuestionContent((String) questionInfo.get("questionContent"));
            statusEntity.setIsEnded((String) questionInfo.get("isEnded"));
            
            // QuestionEntity에서 투표 기간 정보 가져오기
            try {
                QuestionEntity questionEntity = questionService.getQuestionBySerialNumber(serialNumber);
                if (questionEntity != null) {
                    statusEntity.setVotingStartDate(questionEntity.getVotingStartDate());
                    statusEntity.setVotingEndDate(questionEntity.getVotingEndDate());
                    logger.debug("투표 기간 정보 설정 완료: serialNumber={}, 시작={}, 종료={}", 
                                serialNumber, questionEntity.getVotingStartDate(), 
                                questionEntity.getVotingEndDate());
                } else {
                    logger.warn("QuestionEntity를 찾을 수 없음: serialNumber={}", serialNumber);
                }
            } catch (Exception e) {
                logger.error("투표 기간 정보 조회 실패: serialNumber={}, error={}", 
                            serialNumber, e.getMessage(), e);
                // 오류가 발생해도 null로 설정하여 계속 진행
                statusEntity.setVotingStartDate(null);
                statusEntity.setVotingEndDate(null);
            }
            
            // createdAt 타입 변환 처리
            Object createdAtObj = questionInfo.get("createdAt");
            if (createdAtObj instanceof java.sql.Timestamp) {
                statusEntity.setCreatedAt(((java.sql.Timestamp) createdAtObj).toLocalDateTime());
                logger.debug("createdAt 변환 완료 (Timestamp): serialNumber={}", serialNumber);
            } else if (createdAtObj instanceof LocalDateTime) {
                statusEntity.setCreatedAt((LocalDateTime) createdAtObj);
                logger.debug("createdAt 설정 완료 (LocalDateTime): serialNumber={}", serialNumber);
            } else {
                statusEntity.setCreatedAt(null);
                logger.warn("createdAt 값이 예상되지 않은 타입: serialNumber={}, type={}", 
                           serialNumber, createdAtObj != null ? createdAtObj.getClass().getName() : "null");
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
            
            logger.debug("StatusEntity 생성 완료: serialNumber={}, 총투표={}, 투표율={}%", 
                        serialNumber, statusEntity.getTotalVotes(), statusEntity.getVotingRate());
            
            return statusEntity;
            
        } catch (Exception e) {
            logger.error("StatusEntity 생성 실패: serialNumber={}, error={}", 
                        serialNumber, e.getMessage(), e);
            return null;
        }
    }
}