package com.mogatshoo.dev.admin.question.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="completed_question")
@Table(name="completed_question")
@Setter
@Getter
@ToString
public class CompletedQuestionEntity {
    
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
    
    @Column(nullable = false)
    private String isPublic;
    
    @Column(name = "voting_start_date")
    private LocalDateTime votingStartDate;
    
    @Column(name = "voting_end_date")
    private LocalDateTime votingEndDate;
    
    @Column(name = "voting_status")
    private String votingStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at", columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime completedAt;
    
    // QuestionEntity에서 CompletedQuestionEntity로 변환하는 생성자
    public CompletedQuestionEntity(QuestionEntity question) {
        this.serialNumber = question.getSerialNumber();
        this.question = question.getQuestion();
        this.option1 = question.getOption1();
        this.option2 = question.getOption2();
        this.option3 = question.getOption3();
        this.option4 = question.getOption4();
        this.isPublic = question.getIsPublic();
        this.votingStartDate = question.getVotingStartDate();
        this.votingEndDate = question.getVotingEndDate();
        this.votingStatus = question.getVotingStatus();
        this.createdAt = question.getCreatedAt();
        this.completedAt = LocalDateTime.now();
    }
    
    public CompletedQuestionEntity() {}
}