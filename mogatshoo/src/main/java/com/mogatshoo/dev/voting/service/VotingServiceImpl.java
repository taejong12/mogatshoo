package com.mogatshoo.dev.voting.service;

import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.service.QuestionService;
import com.mogatshoo.dev.voting.entity.VotingEntity;
import com.mogatshoo.dev.voting.repository.VotingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class VotingServiceImpl implements VotingService {

    private static final Logger log = LoggerFactory.getLogger(VotingServiceImpl.class);

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private QuestionService questionService;

    @Override
    public QuestionEntity getRandomUnansweredQuestion(String memberId) {
        log.debug("랜덤 미답변 질문 조회 시작 - memberId: {}", memberId);
        
        try {
            // 입력값 검증
            if (memberId == null || memberId.trim().isEmpty()) {
                log.warn("유효하지 않은 memberId: {}", memberId);
                throw new IllegalArgumentException("회원 ID가 유효하지 않습니다.");
            }
            
            // 지역 변수로 선언하여 스레드 안전성 확보
            List<String> votedSerialNumbers = getVotedSerialNumbers(memberId);
            List<QuestionEntity> publicQuestions = getPublicQuestions();
            
            if (publicQuestions.isEmpty()) {
                log.info("공개된 질문이 없습니다.");
                return null;
            }

            // 투표하지 않은 질문만 필터링
            List<QuestionEntity> unansweredQuestions = publicQuestions.stream()
                    .filter(q -> !votedSerialNumbers.contains(q.getSerialNumber()))
                    .collect(Collectors.toList());

            log.info("투표하지 않은 질문 수: {} / 전체 공개 질문 수: {}", 
                    unansweredQuestions.size(), publicQuestions.size());

            if (unansweredQuestions.isEmpty()) {
                log.info("사용자 {}가 모든 질문에 이미 투표했습니다.", memberId);
                return null;
            }

            // 랜덤으로 하나 선택
            QuestionEntity selectedQuestion = selectRandomQuestion(unansweredQuestions);
            log.info("선택된 질문: {} (사용자: {})", selectedQuestion.getSerialNumber(), memberId);
            
            return selectedQuestion;
            
        } catch (IllegalArgumentException e) {
            log.error("입력값 오류 - memberId: {}, 오류: {}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("랜덤 질문 조회 중 예상치 못한 오류 발생 - memberId: {}", memberId, e);
            throw new RuntimeException("질문 조회 중 시스템 오류가 발생했습니다.", e);
        }
    }

    private List<String> getVotedSerialNumbers(String memberId) {
        try {
            List<String> votedSerialNumbers = votingRepository.findSerialNumbersVotedByMember(memberId);
            if (votedSerialNumbers == null) {
                votedSerialNumbers = new ArrayList<>();
            }
            log.debug("사용자 {}가 투표한 질문 수: {}", memberId, votedSerialNumbers.size());
            return votedSerialNumbers;
        } catch (Exception e) {
            log.error("투표한 질문 목록 조회 실패 - memberId: {}", memberId, e);
            throw new RuntimeException("투표 이력 조회 중 오류가 발생했습니다.", e);
        }
    }

    private List<QuestionEntity> getPublicQuestions() {
        try {
            List<QuestionEntity> publicQuestions = questionService.getQuestionsByPublicStatus("yes");
            if (publicQuestions == null) {
                publicQuestions = new ArrayList<>();
            }
            log.debug("공개 질문 총 개수: {}", publicQuestions.size());
            return publicQuestions;
        } catch (Exception e) {
            log.error("공개 질문 조회 실패", e);
            throw new RuntimeException("공개 질문 조회 중 오류가 발생했습니다.", e);
        }
    }

    private QuestionEntity selectRandomQuestion(List<QuestionEntity> questions) {
        if (questions.isEmpty()) {
            throw new IllegalStateException("선택할 질문이 없습니다.");
        }
        
        int randomIndex = new Random().nextInt(questions.size());
        return questions.get(randomIndex);
    }

    @Override
    @Transactional
    public VotingEntity saveVote(String serialNumber, String voterId, String votedId) {
        log.info("투표 저장 시작 - 질문: {}, 투표자: {}, 선택: {}", serialNumber, voterId, votedId);
        
        try {
            // 입력값 유효성 검사
            validateVoteInput(serialNumber, voterId, votedId);
            
            // 중복 투표 검사
            if (hasVoted(serialNumber, voterId)) {
                log.warn("중복 투표 시도 - 질문: {}, 투표자: {}", serialNumber, voterId);
                throw new IllegalStateException("이미 이 질문에 투표하셨습니다.");
            }

            // 새 투표 엔티티 생성 및 저장
            VotingEntity voting = createVotingEntity(serialNumber, voterId, votedId);
            VotingEntity savedVoting = votingRepository.save(voting);
            
            log.info("투표 저장 완료 - ID: {}, 질문: {}, 투표자: {}, 선택: {}", 
                    savedVoting.getId(), serialNumber, voterId, votedId);
            
            return savedVoting;
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("투표 저장 실패 (비즈니스 로직 오류): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("투표 저장 중 시스템 오류 - 질문: {}, 투표자: {}, 선택: {}", 
                     serialNumber, voterId, votedId, e);
            throw new RuntimeException("투표 저장 중 시스템 오류가 발생했습니다.", e);
        }
    }

    private void validateVoteInput(String serialNumber, String voterId, String votedId) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("질문 번호가 유효하지 않습니다.");
        }
        if (voterId == null || voterId.trim().isEmpty()) {
            throw new IllegalArgumentException("투표자 ID가 유효하지 않습니다.");
        }
        if (votedId == null || votedId.trim().isEmpty()) {
            throw new IllegalArgumentException("선택된 옵션이 유효하지 않습니다.");
        }
    }

    private VotingEntity createVotingEntity(String serialNumber, String voterId, String votedId) {
        VotingEntity voting = new VotingEntity();
        voting.setSerialNumber(serialNumber);
        voting.setVoterId(voterId);
        voting.setVotedId(votedId);
        voting.setSelectedOption(votedId);
        return voting;
    }

    @Override
    public Map<String, Long> getVoteCountsByQuestion(String serialNumber) {
        log.debug("질문별 투표 집계 조회 - 질문: {}", serialNumber);
        
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                log.warn("질문 번호가 비어있음");
                return new HashMap<>();
            }
            
            // DB 쿼리로 집계 시도
            Map<String, Long> voteCounts = getVoteCountsFromDatabase(serialNumber);
            log.info("질문 {}의 투표 집계 결과: {}", serialNumber, voteCounts);
            
            return voteCounts;
            
        } catch (Exception e) {
            log.error("투표 집계 조회 실패 - 질문: {}", serialNumber, e);
            return getVoteCountsFromStream(serialNumber);
        }
    }

    private Map<String, Long> getVoteCountsFromDatabase(String serialNumber) {
        try {
            List<Object[]> results = votingRepository.countVotesByQuestionAndVotedId(serialNumber);
            
            Map<String, Long> voteCounts = new HashMap<>();
            for (Object[] result : results) {
                String votedId = (String) result[0];
                Long count = (Long) result[1];
                voteCounts.put(votedId, count);
            }
            
            log.debug("DB 쿼리를 통한 투표 집계 성공 - 질문: {}", serialNumber);
            return voteCounts;
            
        } catch (Exception e) {
            log.warn("DB 쿼리를 통한 투표 집계 실패, 스트림 방식으로 대체 - 질문: {}", serialNumber);
            throw e;
        }
    }

    private Map<String, Long> getVoteCountsFromStream(String serialNumber) {
        try {
            List<VotingEntity> votes = votingRepository.findBySerialNumber(serialNumber);
            Map<String, Long> voteCounts = votes.stream()
                    .collect(Collectors.groupingBy(
                            VotingEntity::getVotedId,
                            Collectors.counting()
                    ));
            
            log.info("스트림을 통한 투표 집계 성공 - 질문: {}", serialNumber);
            return voteCounts;
            
        } catch (Exception e) {
            log.error("스트림을 통한 투표 집계도 실패 - 질문: {}", serialNumber, e);
            return new HashMap<>();
        }
    }

    @Override
    public List<VotingEntity> getVoteHistoryByVoterId(String voterId) {
        log.debug("투표 이력 조회 - 사용자: {}", voterId);
        
        try {
            if (voterId == null || voterId.trim().isEmpty()) {
                log.warn("유효하지 않은 voterId");
                return new ArrayList<>();
            }
            
            List<VotingEntity> history = votingRepository.findByVoterIdOrderByCreatedAtDesc(voterId);
            if (history == null) {
                history = new ArrayList<>();
            }
            
            log.info("사용자 {}의 투표 이력 조회 완료: {}개", voterId, history.size());
            return history;
            
        } catch (Exception e) {
            log.error("투표 이력 조회 실패 - 사용자: {}", voterId, e);
            throw new RuntimeException("투표 이력 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean hasVoted(String serialNumber, String voterId) {
        log.debug("투표 중복 체크 - 질문: {}, 사용자: {}", serialNumber, voterId);
        
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty() || 
                voterId == null || voterId.trim().isEmpty()) {
                log.warn("투표 중복 체크 - 유효하지 않은 파라미터");
                return false;
            }
            
            boolean hasVoted = votingRepository.existsBySerialNumberAndVoterId(serialNumber, voterId);
            log.debug("투표 중복 체크 결과 - 질문: {}, 사용자: {}, 결과: {}", 
                     serialNumber, voterId, hasVoted);
            
            return hasVoted;
            
        } catch (Exception e) {
            log.error("투표 중복 체크 실패 - 질문: {}, 사용자: {}", serialNumber, voterId, e);
            // 안전을 위해 true 반환 (중복 투표 방지)
            log.warn("안전을 위해 중복 투표로 처리");
            return true;
        }
    }
}