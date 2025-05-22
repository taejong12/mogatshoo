package com.mogatshoo.dev.voting.controller;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.question.entity.QuestionEntity;
import com.mogatshoo.dev.question.service.QuestionService;
import com.mogatshoo.dev.voting.entity.VotingEntity;
import com.mogatshoo.dev.voting.service.VotingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/voting")
public class VotingController {

    @Autowired
    private VotingService votingService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private HairLossTestService hairLossTestService;

    /**
     * 투표 페이지 로드 - 랜덤 질문 표시
     */
    @GetMapping
    public String votingPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            // 현재 로그인한 사용자 ID 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentMemberId = authentication.getName();
            
            if (currentMemberId == null || currentMemberId.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
                return "redirect:/login"; // 로그인 페이지로 리다이렉트
            }
            
            // 모델에 현재 사용자 정보 추가
            MemberEntity member = memberService.findByMemberId(currentMemberId);
            model.addAttribute("member", member);
            
            // 해당 사용자가 아직 투표하지 않은 공개 질문 중 랜덤으로 하나 가져오기
            QuestionEntity randomQuestion = votingService.getRandomUnansweredQuestion(currentMemberId);
            
            // 질문이 없는 경우 (모든 질문에 투표했거나 공개 질문이 없는 경우)
            if (randomQuestion == null) {
                System.out.println("질문이 없습니다. 템플릿 경로: question/no-questions");
                model.addAttribute("noMoreQuestions", true);
                model.addAttribute("message", "현재 투표할 질문이 없습니다.");
                return "question/no-questions"; // 템플릿 경로 수정 (실제 파일 위치에 맞게)
            }
            
            // 랜덤 사진 가져오기 (테스트에 사용된 사진 4개)
            List<PictureEntity> randomPictures = hairLossTestService.getRandomPictures(4);
            
            // 사진이 부족한 경우 처리
            if (randomPictures == null || randomPictures.size() < 4) {
                System.out.println("사진이 부족합니다. 템플릿 경로: question/no-questions");
                model.addAttribute("message", "사진이 부족하여 투표할 수 없습니다.");
                return "question/no-questions";
            }
            
            model.addAttribute("question", randomQuestion);
            model.addAttribute("randomPictures", randomPictures);
            
            return "voting/voting";
        } catch (Exception e) {
            System.err.println("투표 페이지 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "투표 시스템 접근 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 투표 제출 처리 후 다음 질문으로 이동
     */
    @PostMapping("/submit")
    public String submitVote(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("votedId") String votedId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String voterId = authentication.getName();
            
            if (voterId == null || voterId.isEmpty()) {
                redirectAttributes.addFlashAttribute("voteError", true);
                redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보를 찾을 수 없습니다.");
                return "redirect:/login";
            }
            
            System.out.println("투표 정보 - 질문: " + serialNumber + ", 투표자: " + voterId + ", 선택: " + votedId);
            
            // 투표 저장
            VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);
            
            if (vote != null && vote.getId() != null) {
                // 성공 메시지 설정
                redirectAttributes.addFlashAttribute("voteSuccess", true);
                redirectAttributes.addFlashAttribute("message", "투표가 성공적으로 저장되었습니다.");
            } else {
                // 저장은 되었지만 ID가 없는 경우 (드문 경우)
                redirectAttributes.addFlashAttribute("voteWarning", true);
                redirectAttributes.addFlashAttribute("message", "투표가 처리되었지만 확인이 필요합니다.");
            }
            
            // 투표 페이지로 리다이렉트 (다음 질문이 자동으로 표시됨)
            return "redirect:/voting";
            
        } catch (Exception e) {
            // 오류 메시지 설정
            System.err.println("투표 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("voteError", true);
            redirectAttributes.addFlashAttribute("errorMessage", "투표 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/voting";
        }
    }
    
    /**
     * AJAX 투표 제출 처리 (선택적)
     */
    @PostMapping("/api/submit")
    @ResponseBody
    public ResponseEntity<?> submitVoteApi(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("votedId") String votedId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String voterId = authentication.getName();
            
            // 투표 저장
            VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);
            
            // 성공 응답
            response.put("success", true);
            response.put("message", "투표가 성공적으로 저장되었습니다.");
            response.put("voteId", vote.getId());
            
            // 다음 질문이 있는지 확인
            QuestionEntity nextQuestion = votingService.getRandomUnansweredQuestion(voterId);
            if (nextQuestion == null) {
                response.put("noMoreQuestions", true);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "투표 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 투표 결과 확인 페이지
     */
    @GetMapping("/results/{serialNumber}")
    public String viewVoteResults(@PathVariable("serialNumber") String serialNumber, Model model) {
        // 현재 로그인한 사용자 정보
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberId = authentication.getName();
        
        // 질문 정보
        QuestionEntity question = questionService.getQuestionBySerialNumber(serialNumber);
        
        // 투표 결과 집계
        Map<String, Long> voteCounts = votingService.getVoteCountsByQuestion(serialNumber);
        
        model.addAttribute("question", question);
        model.addAttribute("voteCounts", voteCounts);
        model.addAttribute("totalVotes", voteCounts.values().stream().mapToLong(Long::longValue).sum());
        
        return "voting/results";
    }
    
    /**
     * 사용자의 투표 이력 보기
     */
    @GetMapping("/history")
    public String viewVoteHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String memberId = authentication.getName();
        
        model.addAttribute("voteHistory", votingService.getVoteHistoryByVoterId(memberId));
        model.addAttribute("member", memberService.findByMemberId(memberId));
        return "voting/history";
    }
}