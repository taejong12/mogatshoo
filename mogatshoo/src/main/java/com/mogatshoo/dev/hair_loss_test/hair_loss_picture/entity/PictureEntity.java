package com.mogatshoo.dev.hair_loss_test.hair_loss_picture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity(name="picture")
@Table(name="picture")
@Setter
@Getter
@ToString
public class PictureEntity {

    @Id
    private String memberId;  // 회원 ID를 PK로 사용

    @Column(nullable = false)
    private String hairPicture;  // 사진 경로 또는 데이터
    
    @Column(updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;  // 최초 생성 시간
    
    @Column(columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;  // 마지막 업데이트 시간
}