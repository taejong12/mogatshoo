package com.mogatshoo.dev.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "agree")
@Setter
@Getter
@ToString
public class AgreeEntity {

	@Id
	private String memberId;

	// 통합 서비스 동의
	@Column(nullable = false)
	private String agreeIntegration;

	// 개인정보 수집동의
	@Column(nullable = false)
	private String agreeInfo;

	// 등록일
	@Column(columnDefinition = "DATETIME", nullable = false)
	private LocalDateTime agreeCreate;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime agreeUpdate;

	// 생성일 설정
	@PrePersist
	protected void onCreate() {
		this.agreeCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.agreeUpdate = LocalDateTime.now();
	}

}
