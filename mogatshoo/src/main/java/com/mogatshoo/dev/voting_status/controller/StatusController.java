package com.mogatshoo.dev.voting_status.controller;

import com.mogatshoo.dev.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.voting_status.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 투표 관리 현황 페이지 (페이지네이션 추가)
    @GetMapping("")
    public String votingStatusPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        try {
            // 페이지네이션 설정 (최신순 정렬)
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            // 페이징된 투표 통계 조회
            Page<StatusEntity> votingStatisticsPage = statusService.getAllVotingStatistics(pageable);
            
            // 데이터가 없는 경우 처리
            if (votingStatisticsPage.isEmpty()) {
                model.addAttribute("noData", true);
                model.addAttribute("message", "현재 등록된 질문이 없습니다.");
                model.addAttribute("votingStatistics", new ArrayList<>());
                model.addAttribute("totalQuestions", 0);
                model.addAttribute("eligibleQuestions", 0);
                model.addAttribute("activeQuestions", 0);
                model.addAttribute("totalMembers", 0);
                
                // 페이지네이션 정보
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
                model.addAttribute("totalElements", 0);
                model.addAttribute("hasNext", false);
                model.addAttribute("hasPrevious", false);
                
                return "admin/voting/status";
            }
            
            List<StatusEntity> votingStatistics = votingStatisticsPage.getContent();
            
            // 기본 통계 정보
            model.addAttribute("votingStatistics", votingStatistics);
            
            // 전체 통계 (페이징과 별도로 계산)
            int totalQuestions = (int) votingStatisticsPage.getTotalElements();
            model.addAttribute("totalQuestions", totalQuestions);
            
            // 투표율 40% 이상이고 종료된 질문 수 계산 (현재 페이지 기준)
            long eligibleQuestions = votingStatistics.stream()
                    .filter(stat -> "yes".equals(stat.getIsEnded()) && 
                                   stat.getVotingRate() != null && stat.getVotingRate() >= 40.0)
                    .count();
            model.addAttribute("eligibleQuestions", eligibleQuestions);
            
            // 진행중인 질문 수 계산 (현재 페이지 기준)
            long activeQuestions = votingStatistics.stream()
                    .filter(stat -> "no".equals(stat.getIsEnded()))
                    .count();
            model.addAttribute("activeQuestions", activeQuestions);
            
            // 전체 회원 수
            if (!votingStatistics.isEmpty()) {
                model.addAttribute("totalMembers", votingStatistics.get(0).getTotalMembers());
            } else {
                model.addAttribute("totalMembers", 0);
            }
            
            // 페이지네이션 정보
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", votingStatisticsPage.getTotalPages());
            model.addAttribute("totalElements", votingStatisticsPage.getTotalElements());
            model.addAttribute("hasNext", votingStatisticsPage.hasNext());
            model.addAttribute("hasPrevious", votingStatisticsPage.hasPrevious());
            model.addAttribute("pageSize", size);
            
            // 페이지 번호 리스트 생성 (현재 페이지 기준 ±2)
            List<Integer> pageNumbers = new ArrayList<>();
            int totalPages = votingStatisticsPage.getTotalPages();
            int startPage = Math.max(0, page - 2);
            int endPage = Math.min(totalPages - 1, page + 2);
            
            for (int i = startPage; i <= endPage; i++) {
                pageNumbers.add(i);
            }
            model.addAttribute("pageNumbers", pageNumbers);
            
            return "admin/voting/status";
            
        } catch (Exception e) {
            System.err.println("투표 현황 페이지 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "투표 현황을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("noData", true);
            
            // 오류 시에도 페이지네이션 기본값 설정
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
            
            return "admin/voting/status";
        }
    }

    // 특정 질문의 상세 통계 조회
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

    // 이메일 전송 페이지로 이동
    @GetMapping("/email/{serialNumber}")
    public String emailPage(@PathVariable String serialNumber, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 해당 질문의 통계 정보 조회
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            if (questionStats == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "질문 정보를 찾을 수 없습니다.");
                return "redirect:/voting-status";
            }
            
            // 투표율 40% 이상인지 확인 (종료된 질문만)
            if (!"yes".equals(questionStats.getIsEnded())) {
                redirectAttributes.addFlashAttribute("errorMessage", "진행 중인 질문은 이메일을 전송할 수 없습니다.");
                return "redirect:/voting-status";
            }
            
            if (questionStats.getVotingRate() < 40.0) {
                redirectAttributes.addFlashAttribute("errorMessage", "투표율이 40% 미만인 질문은 이메일을 전송할 수 없습니다.");
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

    // 투표 통계 새로고침
    @PostMapping("/refresh")
    public String refreshStatistics(
            @RequestParam(value = "page", defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            // 통계 데이터 새로고침 (캐시 클리어 등)
            statusService.refreshStatistics();
            redirectAttributes.addFlashAttribute("successMessage", "투표 통계가 새로고침되었습니다.");
        } catch (Exception e) {
            System.err.println("통계 새로고침 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "통계 새로고침 중 오류가 발생했습니다.");
        }
        return "redirect:/voting-status?page=" + page;
    }
}