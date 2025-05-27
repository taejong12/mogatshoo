package com.mogatshoo.dev.member.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="agree")
@Table(name="agree")
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
	
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDate agreeCreate;
	
	@Column(insertable = false, updatable = true, columnDefinition = "timestamp default current_timestamp ON UPDATE current_timestamp")
	private LocalDate agreeUpdate;
	
}
