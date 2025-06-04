package com.mogatshoo.dev.admin.point.category.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_category")
@Getter
@Setter
@ToString
public class AdminPointCategoryEntity {

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

	// 회원아이디
	@Column(nullable = false)
	private String memberId;

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointCategoryUpdate = LocalDateTime.now();
	}
}
