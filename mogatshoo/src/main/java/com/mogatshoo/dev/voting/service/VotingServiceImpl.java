package com.mogatshoo.dev.voting.service;

import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.service.QuestionService;
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

    @Override
    public QuestionEntity getRandomUnansweredQuestion(String memberId) {
        try {
            // 예외 처리: memberId가 null이거나 비어있는 경우
            if (memberId == null || memberId.trim().isEmpty()) {
                System.err.println("memberId가 유효하지 않습니다: " + memberId);
                return null;
            }
            
            // 지역 변수로 선언하여 스레드 안전성 확보
            List<String> votedSerialNumbers = new ArrayList<>();
            List<QuestionEntity> publicQuestions = new ArrayList<>();
            
            // 해당 사용자가 이미 투표한 질문 일련번호 목록
            try {
                List<String> temp = votingRepository.findSerialNumbersVotedByMember(memberId);
                if (temp != null) {
                    votedSerialNumbers.addAll(temp);
                }
                System.out.println("사용자 " + memberId + "가 투표한 질문 수: " + votedSerialNumbers.size());
            } catch (Exception e) {
                System.err.println("투표한 질문 조회 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }

            // 공개(yes) 상태이면서 투표 가능한 질문만 가져오기
            try {
                List<QuestionEntity> temp = questionService.getQuestionsByPublicStatus("yes");
                if (temp != null) {
                    // 투표 가능한 질문만 필터링 (진행중 상태인 것만)
                    publicQuestions.addAll(temp.stream()
                        .filter(q -> q.isVotingAvailable())
                        .collect(Collectors.toList()));
                }
                System.out.println("투표 가능한 공개 질문 총 개수: " + publicQuestions.size());
            } catch (Exception e) {
                System.err.println("공개 질문 조회 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            if (publicQuestions.isEmpty()) {
                System.err.println("투표 가능한 공개 질문이 없습니다.");
                return null; // 투표 가능한 공개 질문이 없음
            }

            // 투표하지 않은 질문만 필터링
            List<QuestionEntity> unansweredQuestions = publicQuestions.stream()
                    .filter(q -> !votedSerialNumbers.contains(q.getSerialNumber()))
                    .collect(Collectors.toList());

            System.out.println("투표하지 않은 질문 수: " + unansweredQuestions.size());

            if (unansweredQuestions.isEmpty()) {
                System.out.println("모든 질문에 이미 투표했습니다.");
                return null; // 모든 질문에 이미 투표함
            }

            // 랜덤으로 하나 선택
            int randomIndex = new Random().nextInt(unansweredQuestions.size());
            QuestionEntity selectedQuestion = unansweredQuestions.get(randomIndex);
            
            // 질문의 옵션 이미지 경로 검증 및 수정
            selectedQuestion = validateAndFixQuestionOptions(selectedQuestion);
            
            System.out.println("선택된 질문: " + selectedQuestion.getSerialNumber());
            System.out.println("질문 옵션들: ");
            System.out.println("- Option1: " + selectedQuestion.getOption1());
            System.out.println("- Option2: " + selectedQuestion.getOption2());
            System.out.println("- Option3: " + selectedQuestion.getOption3());
            System.out.println("- Option4: " + selectedQuestion.getOption4());
            
            return selectedQuestion;
            
        } catch (Exception e) {
            // 로깅하고 null 반환
            System.err.println("랜덤 질문 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 질문의 옵션 이미지 경로 검증 및 수정
     */
    private QuestionEntity validateAndFixQuestionOptions(QuestionEntity question) {
        if (question == null) return null;
        
        try {
            // 각 옵션의 이미지 경로 검증 및 수정
            question.setOption1(fixImagePath(question.getOption1()));
            question.setOption2(fixImagePath(question.getOption2()));
            question.setOption3(fixImagePath(question.getOption3()));
            question.setOption4(fixImagePath(question.getOption4()));
            
            return question;
        } catch (Exception e) {
            System.err.println("질문 옵션 경로 수정 중 오류: " + e.getMessage());
            return question;
        }
    }
    
    /**
     * 이미지 경로 수정 헬퍼 메서드
     */
    private String fixImagePath(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return imagePath;
        }
        
        // Firebase Storage URL인 경우 그대로 반환
        if (imagePath.startsWith("https://firebasestorage.googleapis.com") || 
            imagePath.startsWith("http://") || 
            imagePath.startsWith("https://")) {
            return imagePath;
        }
        
        // 상대 경로인 경우 절대 경로로 변환
        if (!imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }
        
        // /uploads/ 경로 처리
        if (imagePath.contains("/uploads/") && !imagePath.startsWith("/uploads/")) {
            imagePath = imagePath.substring(imagePath.indexOf("/uploads/"));
        }
        
        return imagePath;
    }

    @Override
    @Transactional
    public VotingEntity saveVote(String serialNumber, String voterId, String votedId) {
        try {
            // 입력값 유효성 검사
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("질문 번호가 유효하지 않습니다.");
            }
            if (voterId == null || voterId.trim().isEmpty()) {
                throw new IllegalArgumentException("투표자 ID가 유효하지 않습니다.");
            }
            if (votedId == null || votedId.trim().isEmpty()) {
                throw new IllegalArgumentException("선택된 옵션이 유효하지 않습니다.");
            }
            
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
            System.out.println("투표가 성공적으로 저장되었습니다: ID=" + savedVoting.getId() + 
                             ", 질문=" + serialNumber + 
                             ", 투표자=" + voterId + 
                             ", 선택=" + votedId);
            
            return savedVoting;
        } catch (IllegalStateException | IllegalArgumentException e) {
            // 비즈니스 로직 예외는 그대로 전파
            System.err.println("투표 저장 중 비즈니스 로직 오류: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("투표 저장 중 시스템 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("투표 저장 중 시스템 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Map<String, Long> getVoteCountsByQuestion(String serialNumber) {
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                return new HashMap<>();
            }
            
            // DB 쿼리로 집계
            List<Object[]> results = votingRepository.countVotesByQuestionAndVotedId(serialNumber);
            
            Map<String, Long> voteCounts = new HashMap<>();
            for (Object[] result : results) {
                String votedId = (String) result[0];
                Long count = (Long) result[1];
                voteCounts.put(votedId, count);
            }
            
            System.out.println("질문 " + serialNumber + "의 투표 집계 결과: " + voteCounts);
            return voteCounts;
            
        } catch (Exception e) {
            // 실패 시 Java 스트림으로 집계 (대체 방법)
            System.err.println("DB 집계 쿼리 실패, Java 스트림으로 집계 시도: " + e.getMessage());
            
            try {
                List<VotingEntity> votes = votingRepository.findBySerialNumber(serialNumber);
                return votes.stream()
                        .collect(Collectors.groupingBy(
                                VotingEntity::getVotedId,
                                Collectors.counting()
                        ));
            } catch (Exception e2) {
                System.err.println("Java 스트림 집계도 실패: " + e2.getMessage());
                return new HashMap<>();
            }
        }
    }

    @Override
    public List<VotingEntity> getVoteHistoryByVoterId(String voterId) {
        try {
            if (voterId == null || voterId.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            List<VotingEntity> history = votingRepository.findByVoterIdOrderByCreatedAtDesc(voterId);
            System.out.println("사용자 " + voterId + "의 투표 이력: " + history.size() + "개");
            return history;
            
        } catch (Exception e) {
            System.err.println("투표 이력 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean hasVoted(String serialNumber, String voterId) {
        try {
            if (serialNumber == null || serialNumber.trim().isEmpty() || 
                voterId == null || voterId.trim().isEmpty()) {
                return false;
            }
            
            boolean hasVoted = votingRepository.existsBySerialNumberAndVoterId(serialNumber, voterId);
            System.out.println("투표 중복 체크 - 질문: " + serialNumber + ", 사용자: " + voterId + ", 결과: " + hasVoted);
            return hasVoted;
            
        } catch (Exception e) {
            System.err.println("투표 중복 체크 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 안전을 위해 true 반환 (중복 투표 방지)
            return true;
        }
    }
}