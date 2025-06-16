package com.mogatshoo.dev.member.entity;

import java.time.LocalDate;
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
@Table(name = "member")
@Setter
@Getter
@ToString
public class MemberEntity {

	@Id
	private String memberId;

	@Column(nullable = false)
	private String memberPwd;

	@Column(nullable = false)
	private String memberName;

	@Column(nullable = false, unique = true)
	private String memberNickName;

	@Column(nullable = false)
	private String memberTel;

	@Column(nullable = false)
	private LocalDate memberBirth;

	@Column(nullable = false, unique = true)
	private String memberEmail;

	@Column(nullable = false)
	private String memberGender;

	@Column(nullable = false)
	private String memberZipcode;

	@Column(nullable = false)
	private String memberAddress1;

	@Column(nullable = false)
	private String memberAddress2;

	// 등록일
	@Column(columnDefinition = "DATETIME", nullable = false)
	private LocalDateTime memberCreate;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime memberUpdate;

	@Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'local'")
	private String provider = "local";

	@Column
	private String providerId;

	@Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
	private String role = "USER";

	// 생성일 설정
	@PrePersist
	protected void onCreate() {
		this.memberCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.memberUpdate = LocalDateTime.now();
	}
}
