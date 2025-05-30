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

    //투표 페이지 로드
    @GetMapping({"", "/voting"})
    public String votingPage(Model model, RedirectAttributes redirectAttributes) {
        try {
            // 현재 로그인한 사용자 ID 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentMemberId = authentication.getName();
            
            // 로그인 체크
            if (currentMemberId == null || currentMemberId.trim().isEmpty() || "anonymousUser".equals(currentMemberId)) {
                redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
                return "redirect:/member/login";
            }
            
            // 사용자 정보 조회
            MemberEntity member = memberService.findByMemberId(currentMemberId);
            if (member == null) {
                redirectAttributes.addFlashAttribute("message", "사용자 정보를 찾을 수 없습니다.");
                return "redirect:/member/login";
            }
            model.addAttribute("member", member);
            
            // 투표하지 않은 랜덤 질문 가져오기
            QuestionEntity randomQuestion = votingService.getRandomUnansweredQuestion(currentMemberId);
            
            if (randomQuestion == null) {
                model.addAttribute("noMoreQuestions", true);
                model.addAttribute("message", "현재 투표할 질문이 없습니다.");
                return "voting/voting";
            }
            
            // 랜덤 사진 4개 가져오기
            List<PictureEntity> randomPictures = null;
            try {
                randomPictures = hairLossTestService.getRandomPictures(4);
            } catch (Exception e) {
                System.err.println("사진 조회 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (randomPictures == null || randomPictures.size() < 4) {
                model.addAttribute("noMoreQuestions", true);
                model.addAttribute("message", "현재 투표 가능한 사진이 부족합니다. (최소 4개 필요)");
                return "voting/voting";
            }
            
            // 이미지 경로 보정
            for (PictureEntity picture : randomPictures) {
                String imagePath = picture.getHairPicture();
                if (imagePath != null && !imagePath.startsWith("http") && !imagePath.startsWith("/")) {
                    picture.setHairPicture("/images/" + imagePath);
                }
            }
            
            model.addAttribute("question", randomQuestion);
            model.addAttribute("randomPictures", randomPictures);
            
            return "voting/voting";
        } catch (Exception e) {
            System.err.println("투표 페이지 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "투표 시스템 접근 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("noMoreQuestions", true);
            return "voting/voting";
        }
    }

    /**
     * 투표 제출 처리 (AJAX용)
     */
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitVote(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("votedId") String votedId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String voterId = authentication.getName();
            
            // 로그인 체크
            if (voterId == null || voterId.isEmpty() || "anonymousUser".equals(voterId)) {
                response.put("success", false);
                response.put("error", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            System.out.println("투표 처리 시작 - 질문: " + serialNumber + ", 투표자: " + voterId + ", 선택: " + votedId);
            
            // 중복 투표 체크
            if (votingService.hasVoted(serialNumber, voterId)) {
                response.put("success", false);
                response.put("error", "이미 이 질문에 투표하셨습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 투표 저장
            VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);
            
            if (vote != null && vote.getId() != null) {
                System.out.println("투표 저장 성공!");
                response.put("success", true);
                response.put("message", "투표가 성공적으로 저장되었습니다.");
                response.put("voteId", vote.getId());
                
                // 다음 질문이 있는지 확인
                QuestionEntity nextQuestion = votingService.getRandomUnansweredQuestion(voterId);
                if (nextQuestion == null) {
                    response.put("noMoreQuestions", true);
                }
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "투표 저장에 실패했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("투표 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "투표 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 일반 폼 제출용 (백업)
     */
    @PostMapping("/submit-form")
    public String submitVoteForm(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam("votedId") String votedId,
            RedirectAttributes redirectAttributes) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String voterId = authentication.getName();
            
            if (voterId == null || voterId.isEmpty() || "anonymousUser".equals(voterId)) {
                redirectAttributes.addFlashAttribute("voteError", true);
                redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보를 찾을 수 없습니다.");
                return "redirect:/member/login";
            }
            
            // 중복 투표 체크
            if (votingService.hasVoted(serialNumber, voterId)) {
                redirectAttributes.addFlashAttribute("voteWarning", true);
                redirectAttributes.addFlashAttribute("message", "이미 이 질문에 투표하셨습니다.");
                return "redirect:/voting";
            }
            
            // 투표 저장
            VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);
            
            if (vote != null && vote.getId() != null) {
                redirectAttributes.addFlashAttribute("voteSuccess", true);
                redirectAttributes.addFlashAttribute("message", "투표가 성공적으로 저장되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("voteError", true);
                redirectAttributes.addFlashAttribute("errorMessage", "투표 저장에 실패했습니다.");
            }
            
            return "redirect:/voting";
            
        } catch (Exception e) {
            System.err.println("투표 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("voteError", true);
            redirectAttributes.addFlashAttribute("errorMessage", "투표 처리 중 오류가 발생했습니다.");
            return "redirect:/voting";
        }
    }
}