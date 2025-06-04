package com.mogatshoo.dev.admin.amdinEmail.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mogatshoo.dev.admin.amdinEmail.entity.AdminEmailEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminEmailRepository extends JpaRepository<AdminEmailEntity, Long> {
    
    /**
     * 특정 질문의 이메일 전송 이력 조회
     */
    List<AdminEmailEntity> findBySerialNumberOrderByCreatedAtDesc(String serialNumber);
    
    /**
     * 특정 당첨자에게 전송된 이메일 이력 조회
     */
    List<AdminEmailEntity> findByWinnerIdOrderByCreatedAtDesc(String winnerId);
    
    /**
     * 특정 질문과 당첨자에 대한 이메일 전송 여부 확인
     */
    boolean existsBySerialNumberAndWinnerId(String serialNumber, String winnerId);
    
    /**
     * 최근 이메일 전송 이력 조회 (관리자별)
     */
    List<AdminEmailEntity> findBySenderIdOrderByCreatedAtDesc(String senderId);
    
    /**
     * 전송 상태별 이메일 조회
     */
    List<AdminEmailEntity> findByEmailStatusOrderByCreatedAtDesc(String emailStatus);
    
    /**
     * 특정 기간 내 전송된 이메일 조회
     */
    @Query("SELECT ae FROM admin_email ae WHERE ae.createdAt BETWEEN :startDate AND :endDate ORDER BY ae.createdAt DESC")
    List<AdminEmailEntity> findByDateRangeOrderByCreatedAtDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 전송 실패한 이메일 재전송 대상 조회
     */
    List<AdminEmailEntity> findByEmailStatusAndCreatedAtAfterOrderByCreatedAtDesc(
            String emailStatus, LocalDateTime afterDate);
    
    /**
     * 특정 질문의 가장 최근 이메일 전송 기록 조회
     */
    Optional<AdminEmailEntity> findTopBySerialNumberOrderByCreatedAtDesc(String serialNumber);
    
    /**
     * 전체 이메일 전송 통계
     */
    @Query("SELECT COUNT(ae) FROM admin_email ae WHERE ae.emailStatus = :status")
    Long countByEmailStatus(@Param("status") String status);
    
    /**
     * 페이징된 이메일 이력 조회 (관리자용)
     */
    Page<AdminEmailEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 특정 관리자의 페이징된 이메일 전송 이력 조회
     */
    Page<AdminEmailEntity> findBySenderIdOrderByCreatedAtDesc(String senderId, Pageable pageable);
    
    /**
     * 성공적으로 전송된 이메일만 조회
     */
    List<AdminEmailEntity> findByEmailStatusAndSerialNumber(String emailStatus, String serialNumber);
}
