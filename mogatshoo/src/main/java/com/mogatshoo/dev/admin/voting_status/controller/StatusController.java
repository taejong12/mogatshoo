package com.mogatshoo.dev.admin.voting_status.controller;

import com.mogatshoo.dev.admin.amdinEmail.service.AdminEmailService;
import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/voting-status")
public class StatusController {

    @Autowired
    private StatusService statusService;
    
    @Autowired
    private AdminEmailService adminEmailService;

    // 기존 투표 관리 현황 페이지 (페이지네이션 추가) - 수정된 부분
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
                model.addAttribute("pendingQuestions", 0);
                model.addAttribute("activeQuestions", 0);
                model.addAttribute("endedQuestions", 0);
                model.addAttribute("eligibleQuestions", 0);
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
            
            // 각 질문별 이메일 전송 여부 확인
            Map<String, Boolean> emailSentStatus = new HashMap<>();
            for (StatusEntity status : votingStatistics) {
                if (status.getTopVotedId() != null) {
                    boolean emailSent = adminEmailService.isDuplicateEmail(
                        status.getSerialNumber(), status.getTopVotedId());
                    emailSentStatus.put(status.getSerialNumber(), emailSent);
                } else {
                    emailSentStatus.put(status.getSerialNumber(), false);
                }
            }
            
            // 기본 통계 정보
            model.addAttribute("votingStatistics", votingStatistics);
            model.addAttribute("emailSentStatus", emailSentStatus);
            
            // 전체 통계 (페이징과 별도로 계산)
            int totalQuestions = (int) votingStatisticsPage.getTotalElements();
            model.addAttribute("totalQuestions", totalQuestions);
            
            // 상태별 통계 계산 (동적 상태 기준)
            long pendingQuestions = votingStatistics.stream()
                    .filter(stat -> "보류".equals(stat.getCurrentVotingStatus()))
                    .count();
            long activeQuestions = votingStatistics.stream()
                    .filter(stat -> "진행중".equals(stat.getCurrentVotingStatus()))
                    .count();
            long endedQuestions = votingStatistics.stream()
                    .filter(stat -> "종료".equals(stat.getCurrentVotingStatus()))
                    .count();
            
            model.addAttribute("pendingQuestions", pendingQuestions);
            model.addAttribute("activeQuestions", activeQuestions);
            model.addAttribute("endedQuestions", endedQuestions);
            
            // 이메일 전송 가능한 질문 수 계산 (종료되고 참여율 40% 이상)
            long eligibleQuestions = votingStatistics.stream()
                    .filter(stat -> "종료".equals(stat.getCurrentVotingStatus()) && 
                                   stat.getParticipationRate() != null && stat.getParticipationRate() >= 40.0)
                    .count();
            model.addAttribute("eligibleQuestions", eligibleQuestions);
            
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

    // 이메일 전송 페이지로 이동 - 참여율 기준으로 수정
    @GetMapping("/email/{serialNumber}")
    public String emailPage(@PathVariable String serialNumber, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 해당 질문의 통계 정보 조회
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            if (questionStats == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "질문 정보를 찾을 수 없습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 종료된 질문인지 확인 (동적 상태 확인)
            if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "투표가 종료된 질문만 이메일을 전송할 수 있습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 참여율 40% 이상인지 확인
            if (questionStats.getParticipationRate() < 40.0) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    String.format("참여율이 40%% 미만입니다. (현재: %.1f%%, 참여자: %d명)", 
                        questionStats.getParticipationRate(), 
                        questionStats.getUniqueVoters()));
                return "redirect:/admin/voting-status";
            }
            
            // 이미 전송했는지 확인
            if (adminEmailService.isDuplicateEmail(questionStats.getSerialNumber(), questionStats.getTopVotedId())) {
                redirectAttributes.addFlashAttribute("warningMessage", "이미 해당 당첨자에게 이메일을 전송했습니다.");
                return "redirect:/admin/email/history/" + serialNumber;
            }
            
            model.addAttribute("questionStats", questionStats);
            model.addAttribute("serialNumber", serialNumber);
            
            return "redirect:/admin/email/send/" + serialNumber;
        } catch (Exception e) {
            System.err.println("이메일 페이지 이동 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이메일 페이지로 이동하는 중 오류가 발생했습니다.");
            return "redirect:/admin/voting-status";
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
        return "redirect:/admin/voting-status?page=" + page;
    }
    
    // 빠른 이메일 전송 API - 참여율 기준으로 수정
    @PostMapping("/quick-email/{serialNumber}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickSendEmail(@PathVariable String serialNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 투표 통계 조회
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            if (questionStats == null) {
                response.put("success", false);
                response.put("message", "질문 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 종료된 질문인지 확인 (동적 상태 확인)
            if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
                response.put("success", false);
                response.put("message", "투표가 종료된 질문만 이메일을 전송할 수 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 참여율 40% 이상인지 확인
            if (questionStats.getParticipationRate() < 40.0) {
                response.put("success", false);
                response.put("message", String.format("참여율이 40%% 미만입니다. (현재: %.1f%%)", 
                    questionStats.getParticipationRate()));
                return ResponseEntity.badRequest().body(response);
            }
            
            // 중복 전송 체크
            if (adminEmailService.isDuplicateEmail(serialNumber, questionStats.getTopVotedId())) {
                response.put("success", false);
                response.put("message", "이미 해당 당첨자에게 이메일을 전송했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "이메일 전송 페이지로 이동합니다.");
            response.put("redirectUrl", "/admin/email/send/" + serialNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("빠른 이메일 전송 체크 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}