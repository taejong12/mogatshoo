package com.mogatshoo.dev.hair_loss_test.hair_loss_stage.entity;

import java.time.LocalDateTime;

import groovy.transform.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name="stage")
@Table(name="stage")
@Setter
@Getter
@ToString
public class StageEntity {
	@Id
	private String memberId;
	
	@Column(nullable = false)
	private String hairStage;
	
	@Column(updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime createAt;
	
	@Column(columnDefinition = "timestamp default current_timestamp on update current_timestamp")
	private LocalDateTime updatedAt;
}
