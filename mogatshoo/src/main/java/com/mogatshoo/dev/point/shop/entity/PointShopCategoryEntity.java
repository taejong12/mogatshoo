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
@Table(name = "point_category")
@Getter
@Setter
@ToString
public class PointShopCategoryEntity {
	// 포인트상품카테고리 아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer pointCategoryId;

	// 카테고리 이름
	@Column(nullable = false, length = 100)
	private String pointCategoryName;

	// 카테고리 정렬 순서
	@Column
	private Integer pointCategorySortOrder;

	// 등록일
	@Column(columnDefinition = "DATETIME", nullable = false)
	private LocalDateTime pointCategoryCreate;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime pointCategoryUpdate;

	// 생성일 설정
	@PrePersist
	protected void onCreate() {
		this.pointCategoryCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointCategoryUpdate = LocalDateTime.now();
	}
}
