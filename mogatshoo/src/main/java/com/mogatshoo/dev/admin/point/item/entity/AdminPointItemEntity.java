package com.mogatshoo.dev.admin.point.item.entity;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "point_item")
@Getter
@Setter
@ToString
public class AdminPointItemEntity {

	// 상품아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointItemId;

	// 상품이름
	@Column(nullable = false, length = 100)
	private String pointItemName;

	// 유의사항
	@Column(columnDefinition = "TEXT")
	private String pointItemDescription;

	// 상품가격
	@Column(nullable = false)
	private Long pointItemPrice;

	// 재고수량
	@Column(nullable = false)
	private Integer pointItemStock;

	// 등록일
	@Column(columnDefinition = "DATETIME", nullable = false)
	private LocalDateTime pointItemCreate;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime pointItemUpdate;

	// 판매여부 (Y/N)
	@Column(nullable = false, length = 1, columnDefinition = "char(1) default 'Y'")
	private String pointItemSaleStatus;

	// 카테고리아이디
	@Column(nullable = false)
	private Integer pointCategoryId;

	// 회원아이디
	@Column(nullable = false)
	private String memberId;

	// 이미지 파일
	@Transient
	private MultipartFile imgFile;

	// 생성일 설정
	@PrePersist
	protected void onCreate() {
		this.pointItemCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointItemUpdate = LocalDateTime.now();
	}
}
