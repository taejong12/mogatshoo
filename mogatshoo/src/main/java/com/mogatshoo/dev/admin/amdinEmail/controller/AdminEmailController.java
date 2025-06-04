package com.mogatshoo.dev.admin.amdinEmail.controller;

import com.mogatshoo.dev.admin.amdinEmail.entity.AdminEmailEntity;
import com.mogatshoo.dev.admin.amdinEmail.service.AdminEmailService;
import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/email")
public class AdminEmailController {
    
    @Autowired
    private AdminEmailService adminEmailService;
    
    @Autowired
    private StatusService statusService;
    
    /**
     * 이메일 전송 페이지
     */
    @GetMapping("/emailSend/{serialNumber}")
    public String emailSendPage(@PathVariable String serialNumber, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 해당 질문의 투표 통계 조회
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            if (questionStats == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "질문 정보를 찾을 수 없습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 투표가 종료되었는지 확인
            if (!"yes".equals(questionStats.getIsEnded())) {
                redirectAttributes.addFlashAttribute("errorMessage", "진행 중인 질문은 이메일을 전송할 수 없습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 투표율 40% 이상인지 확인
            if (questionStats.getVotingRate() == null || questionStats.getVotingRate() < 40.0) {
                redirectAttributes.addFlashAttribute("errorMessage", "투표율이 40% 미만인 질문은 이메일을 전송할 수 없습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 당첨자 정보가 있는지 확인
            if (questionStats.getTopVotedId() == null || questionStats.getTopVotedName() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "당첨자 정보를 찾을 수 없습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 이미 전송했는지 확인
            if (adminEmailService.isDuplicateEmail(serialNumber, questionStats.getTopVotedId())) {
                redirectAttributes.addFlashAttribute("warningMessage", "이미 해당 당첨자에게 이메일을 전송했습니다.");
                // 전송 이력 페이지로 리다이렉트
                return "redirectadmin/email/emailHistory/" + serialNumber;
            }
            
            model.addAttribute("questionStats", questionStats);
            model.addAttribute("serialNumber", serialNumber);
            
            // 이메일 제목 미리보기 생성
            String emailSubject = String.format("%s님 득표율 %.2f%%를 넘겼습니다. 축하드립니다!", 
                                               questionStats.getTopVotedName(), questionStats.getVotingRate());
            model.addAttribute("emailSubject", emailSubject);
            
            return "admin/email/emailSend/";
            
        } catch (Exception e) {
            System.err.println("이메일 전송 페이지 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이메일 전송 페이지로 이동하는 중 오류가 발생했습니다.");
            return "redirect:/admin/voting-status";
        }
    }
    
    /**
     * 이메일 전송 처리
     */
    @PostMapping("/send")
    public String sendEmail(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam(value = "customContent", required = false) String customContent,
            @RequestParam(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 현재 로그인한 관리자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String senderId = authentication.getName();
            
            if (senderId == null || senderId.isEmpty() || "anonymousUser".equals(senderId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                return "redirect:/member/login";
            }
            
            // 투표 통계 정보 재확인
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            if (questionStats == null || questionStats.getVotingRate() < 40.0) {
                redirectAttributes.addFlashAttribute("errorMessage", "이메일 전송 조건을 만족하지 않습니다.");
                return "redirect:/admin/voting-status";
            }
            
            // 이메일 전송
            AdminEmailEntity emailResult = adminEmailService.sendWinnerEmailFromStatus(
                questionStats, senderId, customContent, attachmentFile);
            
            if (emailResult != null && "SENT".equals(emailResult.getEmailStatus())) {
                String successMessage = String.format("%s님에게 당첨 이메일이 성공적으로 전송되었습니다.", 
                                                     questionStats.getTopVotedName());
                redirectAttributes.addFlashAttribute("successMessage", successMessage);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "이메일 전송에 실패했습니다.");
            }
            
            return "redirect:/admin/voting-status";
            
        } catch (Exception e) {
            System.err.println("이메일 전송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            try {
                String errorMessage = URLEncoder.encode("이메일 전송 중 오류가 발생했습니다: " + e.getMessage(), 
                                                      StandardCharsets.UTF_8.toString());
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("errorMessage", "이메일 전송 중 오류가 발생했습니다.");
            }
            
            return "redirect:/admin/email/emailSend/" + serialNumber;
        }
    }
    
    /**
     * AJAX 이메일 전송 (빠른 전송용)
     */
    @PostMapping("/send-ajax")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmailAjax(
            @RequestParam("serialNumber") String serialNumber,
            @RequestParam(value = "customContent", required = false) String customContent,
            @RequestParam(value = "attachmentFile", required = false) MultipartFile attachmentFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 현재 로그인한 관리자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String senderId = authentication.getName();
            
            if (senderId == null || senderId.isEmpty() || "anonymousUser".equals(senderId)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }
            
            // 투표 통계 정보 확인
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            if (questionStats == null) {
                response.put("success", false);
                response.put("message", "질문 정보를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (questionStats.getVotingRate() < 40.0) {
                response.put("success", false);
                response.put("message", "투표율이 40% 미만입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 중복 전송 체크
            if (adminEmailService.isDuplicateEmail(serialNumber, questionStats.getTopVotedId())) {
                response.put("success", false);
                response.put("message", "이미 해당 당첨자에게 이메일을 전송했습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이메일 전송
            AdminEmailEntity emailResult = adminEmailService.sendWinnerEmailFromStatus(
                questionStats, senderId, customContent, attachmentFile);
            
            if (emailResult != null && "SENT".equals(emailResult.getEmailStatus())) {
                response.put("success", true);
                response.put("message", questionStats.getTopVotedName() + "님에게 당첨 이메일이 전송되었습니다.");
                response.put("emailId", emailResult.getId());
            } else {
                response.put("success", false);
                response.put("message", "이메일 전송에 실패했습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("AJAX 이메일 전송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "이메일 전송 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 이메일 전송 이력 조회
     */
    @GetMapping("/history/{serialNumber}")
    public String emailHistory(@PathVariable String serialNumber, Model model) {
        try {
            List<AdminEmailEntity> emailHistory = adminEmailService.getEmailHistoryByQuestion(serialNumber);
            StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);
            
            model.addAttribute("emailHistory", emailHistory);
            model.addAttribute("questionStats", questionStats);
            model.addAttribute("serialNumber", serialNumber);
            
            return "admin/email/emailHistory";
            
        } catch (Exception e) {
            System.err.println("이메일 이력 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "이메일 이력을 불러오는 중 오류가 발생했습니다.");
            return "admin/email/emailHistory";
        }
    }
    
    /**
     * 이메일 재전송
     */
    @PostMapping("/resend/{emailId}")
    public String resendEmail(@PathVariable Long emailId, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String senderId = authentication.getName();
            
            AdminEmailEntity resendResult = adminEmailService.resendFailedEmail(emailId, senderId);
            
            if ("SENT".equals(resendResult.getEmailStatus())) {
                redirectAttributes.addFlashAttribute("successMessage", "이메일이 성공적으로 재전송되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "이메일 재전송에 실패했습니다.");
            }
            
            return "redirect:/admin/email/emailHistory/" + resendResult.getSerialNumber();
            
        } catch (Exception e) {
            System.err.println("이메일 재전송 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "이메일 재전송 중 오류가 발생했습니다.");
            return "redirect:/admin/voting-status";
        }
    }
    
    /**
     * 이메일 전송 통계
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<AdminEmailService.EmailStatistics> getEmailStatistics() {
        try {
            AdminEmailService.EmailStatistics statistics = adminEmailService.getEmailStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("이메일 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}