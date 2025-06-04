package com.mogatshoo.dev.point.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime pointCategoryCreate;

	// 수정일
	@Column(insertable = false, updatable = true, columnDefinition = "timestamp default current_timestamp on update current_timestamp")
	private LocalDateTime pointCategoryUpdate;
}
