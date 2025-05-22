package com.mogatshoo.dev.voting.service;

import com.mogatshoo.dev.question.entity.QuestionEntity;
import com.mogatshoo.dev.question.service.QuestionService;
import com.mogatshoo.dev.voting.entity.VotingEntity;
import com.mogatshoo.dev.voting.repository.VotingRepository;

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

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private QuestionService questionService;
    
    // 클래스 필드로 선언 - 주의: 멀티스레드 환경에서 문제가 될 수 있음
    private List<String> votedSerialNumbers = new ArrayList<>();
    private List<QuestionEntity> publicQuestions = new ArrayList<>();

    @Override
    public QuestionEntity getRandomUnansweredQuestion(String memberId) {
        try {
            // 예외 처리: memberId가 null이거나 비어있는 경우
            if (memberId == null || memberId.trim().isEmpty()) {
                System.err.println("memberId가 유효하지 않습니다: " + memberId);
                return null;
            }
            
            // 필드 초기화
            votedSerialNumbers.clear();
            publicQuestions.clear();
            
            // 해당 사용자가 이미 투표한 질문 일련번호 목록
            try {
                List<String> temp = votingRepository.findSerialNumbersVotedByMember(memberId);
                if (temp != null) {
                    votedSerialNumbers.addAll(temp);
                }
            } catch (Exception e) {
                System.err.println("투표한 질문 조회 중 오류 발생: " + e.getMessage());
            }

            // 공개(yes) 상태의 질문만 가져오기
            try {
                List<QuestionEntity> temp = questionService.getQuestionsByPublicStatus("yes");
                if (temp != null) {
                    publicQuestions.addAll(temp);
                }
            } catch (Exception e) {
                System.err.println("공개 질문 조회 중 오류 발생: " + e.getMessage());
            }

            if (publicQuestions.isEmpty()) {
                System.err.println("공개 질문이 없습니다.");
                return null; // 공개 질문이 없음
            }

            // 투표하지 않은 질문만 필터링 - 이제 클래스 필드를 사용하므로 람다에서 문제 없음
            List<QuestionEntity> unansweredQuestions = publicQuestions.stream()
                    .filter(q -> !votedSerialNumbers.contains(q.getSerialNumber()))
                    .collect(Collectors.toList());

            if (unansweredQuestions.isEmpty()) {
                System.err.println("모든 질문에 이미 투표했습니다.");
                return null; // 모든 질문에 이미 투표함
            }

            // 랜덤으로 하나 선택
            int randomIndex = new Random().nextInt(unansweredQuestions.size());
            return unansweredQuestions.get(randomIndex);
        } catch (Exception e) {
            // 로깅하고 null 반환
            System.err.println("랜덤 질문 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public VotingEntity saveVote(String serialNumber, String voterId, String votedId) {
        try {
            // 이미 투표했는지 확인
            if (hasVoted(serialNumber, voterId)) {
                throw new IllegalStateException("이미 이 질문에 투표하셨습니다.");
            }

            // 새 투표 엔티티 생성
            VotingEntity voting = new VotingEntity();
            voting.setSerialNumber(serialNumber);
            voting.setVoterId(voterId);
            voting.setVotedId(votedId);
            
            // 선택된 옵션 설정 (votedId를 선택된 옵션으로 사용)
            voting.setSelectedOption(votedId);

            // 저장 및 반환
            VotingEntity savedVoting = votingRepository.save(voting);
            
            // 저장 확인 로그
            System.out.println("투표가 성공적으로 저장되었습니다: " + savedVoting);
            
            return savedVoting;
        } catch (Exception e) {
            System.err.println("투표 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외 다시 던지기
        }
    }

    @Override
    public Map<String, Long> getVoteCountsByQuestion(String serialNumber) {
        try {
            // DB 쿼리로 집계
            List<Object[]> results = votingRepository.countVotesByQuestionAndVotedId(serialNumber);
            
            Map<String, Long> voteCounts = new HashMap<>();
            for (Object[] result : results) {
                String votedId = (String) result[0];
                Long count = (Long) result[1];
                voteCounts.put(votedId, count);
            }
            
            return voteCounts;
        } catch (Exception e) {
            // 실패 시 Java 스트림으로 집계 (대체 방법)
            System.err.println("DB 집계 쿼리 실패, Java 스트림으로 집계 시도: " + e.getMessage());
            List<VotingEntity> votes = votingRepository.findBySerialNumber(serialNumber);
            return votes.stream()
                    .collect(Collectors.groupingBy(
                            VotingEntity::getVotedId,
                            Collectors.counting()
                    ));
        }
    }

    @Override
    public List<VotingEntity> getVoteHistoryByVoterId(String voterId) {
        return votingRepository.findByVoterIdOrderByCreatedAtDesc(voterId);
    }

    @Override
    public boolean hasVoted(String serialNumber, String voterId) {
        return votingRepository.existsBySerialNumberAndVoterId(serialNumber, voterId);
    }
}