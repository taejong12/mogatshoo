package com.mogatshoo.dev.point.shop.entity;

import java.time.LocalDateTime;

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
import lombok.ToString;

@Entity
@Table(name = "point_item_img")
@Getter
@Setter
@ToString
public class PointShopImgEntity {
	// 포인트 상품 이미지 아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointItemImgId;

	// 이미지 파일 이름
	@Column(nullable = false)
	private String pointItemImgName;

	// 이미지 파일 경로
	@Column(nullable = false)
	private String pointItemImgPath;

	// 등록일
	@Column(columnDefinition = "DATETIME", nullable = false)
	private LocalDateTime pointItemImgCreate;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime pointItemImgUpdate;

	// 상품아이디
	@Column(nullable = false)
	private Long pointItemId;

	// 이미지 URL
	@Column(name = "point_item_img_url", nullable = false)
	private String pointItemImgURL;

	// 생성일 설정
	@PrePersist
	protected void onCreate() {
		this.pointItemImgCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointItemImgUpdate = LocalDateTime.now();
	}
}
