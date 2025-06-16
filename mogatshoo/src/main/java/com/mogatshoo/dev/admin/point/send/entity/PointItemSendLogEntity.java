package com.mogatshoo.dev.admin.point.send.entity;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "point_item_send_log")
@Getter
@Setter
@ToString
public class PointItemSendLogEntity {

	// 로그아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointItemSendLogId;

	// 발송인(관리자)
	@Column(nullable = false, length = 50)
	private String adminId;

	// 수신인(회원)
	@Column(nullable = false, length = 50)
	private String memberId;

	// 회원이메일
	@Column(nullable = false, length = 100)
	private String memberEmail;

	// 이미지 파일 이름
	@Column(nullable = false)
	private String imgName;

	// 이미지 파일 경로
	@Column(nullable = false)
	private String imgPath;

	// 이미지 URL
	@Column(name = "img_url", nullable = false)
	private String imgURL;

	// create 발송일
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime pointItemSendLogCreate;

	// 구매내역 아이디
	@Column(nullable = false)
	private Long pointOrderHistoryId;

	// 이미지 파일 임시 저장
	@Transient
	private MultipartFile imgFile;

}
