package com.mogatshoo.dev.admin.question.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="question")
@Table(name="question")
@Setter
@Getter
@ToString
public class QuestionEntity {
    
    @Id
    private String serialNumber;
    
    @Column(nullable = false)
    private String question;
    
    @Column(nullable = false)
    private String option1;
    
    @Column(nullable = false)
    private String option2;
    
    @Column(nullable = false)
    private String option3;
    
    @Column(nullable = false)
    private String option4;
    
    @Column(nullable = false, columnDefinition = "VARCHAR(3) DEFAULT 'no'")
    private String isPublic = "no";
    
    // 투표 기간 필드 추가
    @Column(name = "voting_start_date")
    private LocalDateTime votingStartDate;
    
    @Column(name = "voting_end_date")
    private LocalDateTime votingEndDate;
    
    // 투표 상태 필드 추가 (진행중, 종료)
    @Column(name = "voting_status", columnDefinition = "VARCHAR(10) DEFAULT '진행중'")
    private String votingStatus = "진행중";
    
    @Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;
}