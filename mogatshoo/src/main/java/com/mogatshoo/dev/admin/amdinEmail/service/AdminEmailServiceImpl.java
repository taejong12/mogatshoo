package com.mogatshoo.dev.admin.amdinEmail.service;

import com.mogatshoo.dev.admin.amdinEmail.entity.AdminEmailEntity;
import com.mogatshoo.dev.admin.amdinEmail.repository.AdminEmailRepository;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.service.StatusService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AdminEmailServiceImpl implements AdminEmailService {
    
    @Autowired
    private AdminEmailRepository adminEmailRepository;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private StatusService statusService;
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${file.upload.path:uploads/attachments}")
    private String uploadPath;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public AdminEmailEntity sendWinnerEmail(String serialNumber, String winnerId, String senderId, 
                                           String customContent, MultipartFile attachmentFile) {
        try {
            System.out.println("=== 이메일 전송 시작 ===");
            System.out.println("질문번호: " + serialNumber);
            System.out.println("당첨자: " + winnerId);
            System.out.println("전송자: " + senderId);
            
            // 중복 전송 체크
            if (isDuplicateEmail(serialNumber, winnerId)) {
                throw new RuntimeException("이미 해당 당첨자에게 이메일을 전송했습니다.");
            }
            
            // 당첨자 정보 조회
            MemberEntity winner = memberService.findByMemberId(winnerId);
            if (winner == null) {
                throw new RuntimeException("당첨자 정보를 찾을 수 없습니다: " + winnerId);
            }
            
            // 투표 통계 정보 조회
            StatusEntity statusEntity = statusService.getQuestionStatistics(serialNumber);
            if (statusEntity == null) {
                throw new RuntimeException("투표 통계 정보를 찾을 수 없습니다: " + serialNumber);
            }
            
            // 투표율 40% 이상인지 확인
            if (statusEntity.getVotingRate() < 40.0) {
                throw new RuntimeException("투표율이 40% 미만입니다: " + statusEntity.getVotingRate() + "%");
            }
            
            // 이메일 엔티티 생성
            AdminEmailEntity emailEntity = createEmailEntity(statusEntity, winner, senderId, customContent);
            
            // 첨부파일 처리
            if (attachmentFile != null && !attachmentFile.isEmpty()) {
                String attachmentPath = saveAttachment(attachmentFile);
                emailEntity.setAttachmentPath(attachmentPath);
                emailEntity.setAttachmentOriginalName(attachmentFile.getOriginalFilename());
            }
            
            // 데이터베이스에 먼저 저장
            AdminEmailEntity savedEntity = adminEmailRepository.save(emailEntity);
            System.out.println("이메일 엔티티 저장 완료: " + savedEntity.getId());
            
            // 이메일 전송
            boolean emailSent = sendEmail(savedEntity, attachmentFile);
            
            // 전송 결과에 따라 상태 업데이트
            if (emailSent) {
                savedEntity.setEmailStatus("SENT");
                savedEntity.setSentAt(LocalDateTime.now());
                System.out.println("이메일 전송 성공: " + winner.getMemberEmail());
            } else {
                savedEntity.setEmailStatus("FAILED");
                savedEntity.setFailureReason("이메일 전송 실패");
                System.err.println("이메일 전송 실패: " + winner.getMemberEmail());
            }
            
            return adminEmailRepository.save(savedEntity);
            
        } catch (Exception e) {
            System.err.println("이메일 전송 프로세스 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 실패한 경우에도 로그를 남기기 위해 엔티티 저장
            try {
                MemberEntity winner = memberService.findByMemberId(winnerId);
                StatusEntity statusEntity = statusService.getQuestionStatistics(serialNumber);
                
                if (winner != null && statusEntity != null) {
                    AdminEmailEntity failedEntity = createEmailEntity(statusEntity, winner, senderId, customContent);
                    failedEntity.setEmailStatus("FAILED");
                    failedEntity.setFailureReason(e.getMessage());
                    if (customContent != null) {
                        failedEntity.setCustomContent(customContent);
                    }
                    return adminEmailRepository.save(failedEntity);
                }
            } catch (Exception saveException) {
                System.err.println("실패 로그 저장 중 오류: " + saveException.getMessage());
            }
            
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public AdminEmailEntity sendWinnerEmailFromStatus(StatusEntity statusEntity, String senderId,
                                                     String customContent, MultipartFile attachmentFile) {
        return sendWinnerEmail(statusEntity.getSerialNumber(), statusEntity.getTopVotedId(), 
                              senderId, customContent, attachmentFile);
    }
    
    private AdminEmailEntity createEmailEntity(StatusEntity statusEntity, MemberEntity winner, 
                                              String senderId, String customContent) {
        AdminEmailEntity emailEntity = new AdminEmailEntity();
        
        // 기본 정보 설정
        emailEntity.setSerialNumber(statusEntity.getSerialNumber());
        emailEntity.setWinnerId(winner.getMemberId());
        emailEntity.setWinnerName(winner.getMemberName());
        emailEntity.setWinnerEmail(winner.getMemberEmail());
        emailEntity.setQuestionContent(statusEntity.getQuestionContent());
        emailEntity.setVotingRate(statusEntity.getVotingRate());
        emailEntity.setVoteCount(statusEntity.getTopVoteCount());
        emailEntity.setSenderId(senderId);
        
        // 사용자 정의 내용 저장
        if (customContent != null && !customContent.trim().isEmpty()) {
            emailEntity.setCustomContent(customContent);
        }
        
        // 이메일 제목 생성 (요구사항에 맞게 수정)
        String subject = String.format("%s님 득표율 %.2f%%를 넘겼습니다. 축하드립니다!", 
                                     winner.getMemberName(), statusEntity.getVotingRate());
        emailEntity.setEmailSubject(subject);
        
        // 이메일 내용 생성
        String emailContent = createEmailContent(statusEntity, winner, customContent);
        emailEntity.setEmailContent(emailContent);
        
        return emailEntity;
    }
    
    private String createEmailContent(StatusEntity statusEntity, MemberEntity winner, String customContent) {
        StringBuilder content = new StringBuilder();
        
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        // 헤더
        content.append("<div style='background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>");
        content.append("<h1 style='margin: 0; font-size: 24px;'>🎉 투표 당첨 축하드립니다! 🎉</h1>");
        content.append("</div>");
        
        // 메인 내용
        content.append("<div style='background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px;'>");
        content.append("<div style='background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        
        content.append("<h2 style='color: #1e293b; margin-top: 0;'>당첨자 정보</h2>");
        content.append("<table style='width: 100%; border-collapse: collapse;'>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>아이디:</td><td>").append(winner.getMemberId()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>이름:</td><td>").append(winner.getMemberName()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>이메일:</td><td>").append(winner.getMemberEmail()).append("</td></tr>");
        content.append("</table>");
        content.append("</div>");
        
        content.append("<div style='background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        content.append("<h2 style='color: #1e293b; margin-top: 0;'>투표 결과</h2>");
        content.append("<table style='width: 100%; border-collapse: collapse;'>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>질문:</td><td>").append(statusEntity.getQuestionContent()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>득표율:</td><td><strong style='color: #10b981; font-size: 18px;'>").append(statusEntity.getFormattedVotingRate()).append("</strong></td></tr>");
        content.append("<tr><td style='padding: 8px 0; font-weight: bold; color: #4f46e5;'>득표수:</td><td>").append(statusEntity.getTopVoteCount()).append("표</td></tr>");
        content.append("</table>");
        content.append("</div>");
        
        // 사용자 정의 당첨 내용
        if (customContent != null && !customContent.trim().isEmpty()) {
            content.append("<div style='background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
            content.append("<h2 style='color: #1e293b; margin-top: 0;'>당첨 안내</h2>");
            content.append("<div style='background: #eff6ff; padding: 15px; border-radius: 8px; border-left: 4px solid #3b82f6;'>");
            content.append(customContent.replace("\n", "<br>"));
            content.append("</div>");
            content.append("</div>");
        }
        
        // 푸터
        content.append("<div style='text-align: center; color: #64748b; font-size: 14px; margin-top: 30px;'>");
        content.append("<p>이 이메일은 Mogatshoo 투표 시스템에서 자동으로 발송되었습니다.</p>");
        content.append("<p>발송일시: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))).append("</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    private boolean sendEmail(AdminEmailEntity emailEntity, MultipartFile attachmentFile) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(emailEntity.getWinnerEmail());
            helper.setSubject(emailEntity.getEmailSubject());
            helper.setText(emailEntity.getEmailContent(), true);
            
            // 첨부파일 추가
            if (attachmentFile != null && !attachmentFile.isEmpty()) {
                helper.addAttachment(attachmentFile.getOriginalFilename(), attachmentFile);
            } else if (emailEntity.getAttachmentPath() != null) {
                File attachmentFileFromPath = new File(emailEntity.getAttachmentPath());
                if (attachmentFileFromPath.exists()) {
                    String originalName = emailEntity.getAttachmentOriginalName();
                    if (originalName == null) {
                        originalName = attachmentFileFromPath.getName();
                    }
                    helper.addAttachment(originalName, attachmentFileFromPath);
                }
            }
            
            javaMailSender.send(message);
            System.out.println("JavaMailSender를 통한 이메일 전송 완료");
            return true;
            
        } catch (Exception e) {
            System.err.println("이메일 전송 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AdminEmailEntity updateEmailStatus(Long emailId, String status, String failureReason) {
        AdminEmailEntity emailEntity = adminEmailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("이메일 기록을 찾을 수 없습니다: " + emailId));
        
        emailEntity.setEmailStatus(status);
        
        if ("SENT".equals(status)) {
            emailEntity.setSentAt(LocalDateTime.now());
        } else if ("FAILED".equals(status) && failureReason != null) {
            emailEntity.setFailureReason(failureReason);
        }
        
        return adminEmailRepository.save(emailEntity);
    }

    @Override
    public List<AdminEmailEntity> getEmailHistoryByQuestion(String serialNumber) {
        return adminEmailRepository.findBySerialNumberOrderByCreatedAtDesc(serialNumber);
    }

    @Override
    public List<AdminEmailEntity> getEmailHistoryByWinner(String winnerId) {
        return adminEmailRepository.findByWinnerIdOrderByCreatedAtDesc(winnerId);
    }

    @Override
    public List<AdminEmailEntity> getEmailHistoryBySender(String senderId) {
        return adminEmailRepository.findBySenderIdOrderByCreatedAtDesc(senderId);
    }

    @Override
    public boolean isDuplicateEmail(String serialNumber, String winnerId) {
        // 성공적으로 전송된 이메일이 있는지 확인
        List<AdminEmailEntity> sentEmails = adminEmailRepository.findByEmailStatusAndSerialNumber("SENT", serialNumber);
        return sentEmails.stream().anyMatch(email -> email.getWinnerId().equals(winnerId));
    }

    @Override
    public AdminEmailEntity resendFailedEmail(Long emailId, String senderId) {
        AdminEmailEntity emailEntity = adminEmailRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("이메일 기록을 찾을 수 없습니다: " + emailId));
        
        if (!"FAILED".equals(emailEntity.getEmailStatus())) {
            throw new RuntimeException("전송 실패한 이메일만 재전송 가능합니다.");
        }
        
        // 새로운 이메일 엔티티 생성 (재전송용)
        AdminEmailEntity resendEntity = new AdminEmailEntity();
        resendEntity.setSerialNumber(emailEntity.getSerialNumber());
        resendEntity.setWinnerId(emailEntity.getWinnerId());
        resendEntity.setWinnerName(emailEntity.getWinnerName());
        resendEntity.setWinnerEmail(emailEntity.getWinnerEmail());
        resendEntity.setQuestionContent(emailEntity.getQuestionContent());
        resendEntity.setVotingRate(emailEntity.getVotingRate());
        resendEntity.setVoteCount(emailEntity.getVoteCount());
        resendEntity.setEmailSubject(emailEntity.getEmailSubject());
        resendEntity.setEmailContent(emailEntity.getEmailContent());
        resendEntity.setCustomContent(emailEntity.getCustomContent());
        resendEntity.setAttachmentPath(emailEntity.getAttachmentPath());
        resendEntity.setAttachmentOriginalName(emailEntity.getAttachmentOriginalName());
        resendEntity.setSenderId(senderId);
        
        AdminEmailEntity saved = adminEmailRepository.save(resendEntity);
        
        // 이메일 재전송 시도
        boolean emailSent = sendEmail(saved, null);
        
        if (emailSent) {
            saved.setEmailStatus("SENT");
            saved.setSentAt(LocalDateTime.now());
        } else {
            saved.setEmailStatus("FAILED");
            saved.setFailureReason("재전송 실패");
        }
        
        return adminEmailRepository.save(saved);
    }

    @Override
    public EmailStatistics getEmailStatistics() {
        Long totalSent = adminEmailRepository.countByEmailStatus("SENT");
        Long totalPending = adminEmailRepository.countByEmailStatus("PENDING");
        Long totalFailed = adminEmailRepository.countByEmailStatus("FAILED");
        
        return new EmailStatistics(totalSent, totalPending, totalFailed);
    }

    @Override
    public String saveAttachment(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return null;
            }
            
            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 파일명 생성 (UUID + 원본 파일명)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            System.out.println("첨부파일 저장 완료: " + filePath.toString());
            return filePath.toString();
            
        } catch (IOException e) {
            System.err.println("첨부파일 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("첨부파일 저장 실패", e);
        }
    }
}