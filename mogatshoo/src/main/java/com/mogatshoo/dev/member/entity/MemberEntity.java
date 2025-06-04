package com.mogatshoo.dev.member.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="member")
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
	
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDate memberCreate;
	
	@Column(insertable = false, updatable = true, columnDefinition = "timestamp default current_timestamp ON UPDATE current_timestamp")
	private LocalDate memberUpdate;
	
	@Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'local'")
	private String provider = "local";
	
	@Column
	private String providerId;
	
	@Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
	private String role = "USER";
}
