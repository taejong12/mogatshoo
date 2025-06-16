package com.mogatshoo.dev.fortune.entity;

import java.time.LocalDateTime;

import groovy.transform.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="fortune")
@Getter
@Setter
@ToString
public class FortuneEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer fortuneId;
	
	@Column
	private String fortuneContent;
	
	@Column
	private String fortuneItem;
	
	// 등록일
		@Column(columnDefinition = "DATETIME", nullable = false)
		private LocalDateTime fortuneCreate;

		// 수정일
		@Column(columnDefinition = "DATETIME")
		private LocalDateTime fortuneUpdate;
	
		// 생성일 설정
		@PrePersist
		protected void onCreate() {
			this.fortuneCreate = LocalDateTime.now();
		}
		
	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.fortuneUpdate = LocalDateTime.now();
	}
	
}
