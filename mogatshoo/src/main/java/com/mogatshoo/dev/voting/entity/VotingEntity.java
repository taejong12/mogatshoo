package com.mogatshoo.dev.voting.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="voting")
@Table(name="voting")
@Setter
@Getter
@ToString
public class VotingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serialNumber; // QuestionEntity의 serialNumber

    @Column(nullable = false)
    private String voterId; // 투표한 회원 ID (MemberEntity에서 가져옴)

    @Column(nullable = false)
    private String votedId; // 투표 받은 회원 ID (PictureEntity에서 가져옴)
    
    @Column(nullable = false)
    private String selectedOption; // 선택한 옵션

    @Column(columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}