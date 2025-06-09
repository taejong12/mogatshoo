package com.mogatshoo.dev.admin.qanda.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.qanda.repository.QandARepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QandACleanupService {
    
    @Autowired
    private QandARepository qandARepository;
    
    // 매월 1일 새벽 2시에 실행
    @Scheduled(cron = "0 0 2 1 * ?")
    public void cleanupOldMessages() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusYears(2);
            
            // 2년 이전 메시지 개수 확인
            long count = qandARepository.countByCreatedAtBefore(cutoffDate);
            
            if (count > 0) {
                log.info("2년 이전 메시지 {}개 삭제 시작", count);
                
                // 삭제 실행
                int deletedCount = qandARepository.deleteByCreatedAtBefore(cutoffDate);
                
                log.info("메시지 {}개 삭제 완료", deletedCount);
            } else {
                log.info("삭제할 오래된 메시지가 없습니다");
            }
            
        } catch (Exception e) {
            log.error("메시지 정리 중 오류 발생", e);
        }
    }
    
    // 테스트용 - 수동 실행
    public void manualCleanup() {
        cleanupOldMessages();
    }
}
