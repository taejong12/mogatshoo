package com.mogatshoo.dev.voting_status.controller;

import com.mogatshoo.dev.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.voting_status.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/voting-status")
public class StatusController {

    @Autowired
    private StatusService statusService;

     //투표 관리 현황 페이지
    @GetMapping("")
    public String votingStatusPage(Model model) {
        try {
            // 모든 질문별 투표 통계 조회
            List<StatusEntity> votingStatistics = statusService.getAllVotingStatistics();
            
            // 데이터가 없는 경우 처리
            if (votingStatistics.isEmpty()) {
                model.addAttribute("noData", true);
                model.addAttribute("message", "현재 등록된 질문이 없습니다.");
                model.addAttribute("votingStatistics", new ArrayList<>());
                model.addAttribute("totalQuestions", 0);
                model.addAttribute("eligibleQuestions", 0);
                model.addAttribute("activeQuestions", 0);
                model.addAttribute("totalMembers", 0);
                return "admin/status";
            }
            
            model.addAttribute("votingStatistics", votingStatistics);
            model.addAttribute("totalQuestions", votingStatistics.size());
            
            // 투표율 1/3 이상인 질문 수 계산
            long eligibleQuestions = votingStatistics.stream()
                    .filter(stat -> stat.getVotingRate() >= 33.33)
                    .count();
            model.addAttribute("eligibleQuestions", eligibleQuestions);
            
            // 진행중인 질문 수 계산
            long activeQuestions = votingStatistics.stream()
                    .filter(stat -> "no".equals(stat.getIsEnded()))
                    .count();
            model.addAttribute("activeQuestions", activeQuestions);
            
            // 전체 회원 수
            model.addAttribute("totalMembers", votingStatistics.get(0).getTotalMembers());
            
            return "admin/status";
            
        } catch (Exception e) {
            System.err.println("투표 현황 페이지 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "투표 현황을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("noData", true);
            return "admin/status";
        }
    }

    //특정 질문의 상세 통계 조회
    @GetMapping("/detail/{serialNumber}")
    @ResponseBody
    public StatusEntity getQuestionDetail(@PathVariable String serialNumber) {
        try {
            return statusService.getQuestionStatistics(serialNumber);
        } catch (Exception e) {
            System.err.println("질문 상세 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //이메일 전송 페이지로 이동
    @GetMapping("/email/{serialNumber}")
    public String emailPage(@PathVariable String serialNumber, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 해당 질문의 통계 정보 조회
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            if (questionStats == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "질문 정보를 찾을 수 없습니다.");
                return "redirect:/voting-status";
            }
            
            // 투표율 1/3 이상인지 확인
            if (questionStats.getVotingRate() < 33.33) {
                redirectAttributes.addFlashAttribute("errorMessage", "투표율이 1/3 미만인 질문은 이메일을 전송할 수 없습니다.");
                return "redirect:/voting-status";
            }
            
            model.addAttribute("questionStats", questionStats);
            model.addAttribute("serialNumber", serialNumber);
            
            return "admin/email";
        } catch (Exception e) {
            System.err.println("이메일 페이지 이동 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이메일 페이지로 이동하는 중 오류가 발생했습니다.");
            return "redirect:/voting-status";
        }
    }

	//투표 통계 새로고침
    @PostMapping("/refresh")
    public String refreshStatistics(RedirectAttributes redirectAttributes) {
        try {
            // 통계 데이터 새로고침 (캐시 클리어 등)
            statusService.refreshStatistics();
            redirectAttributes.addFlashAttribute("successMessage", "투표 통계가 새로고침되었습니다.");
        } catch (Exception e) {
            System.err.println("통계 새로고침 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "통계 새로고침 중 오류가 발생했습니다.");
        }
        return "redirect:/voting-status";
    }
}
