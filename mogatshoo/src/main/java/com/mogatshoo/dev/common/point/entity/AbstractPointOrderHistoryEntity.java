package com.mogatshoo.dev.common.point.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class AbstractPointOrderHistoryEntity {

	// 구매내역아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long pointOrderHistoryId;

	// 구매수량
	@Column(nullable = false)
	protected int pointOrderHistoryQuantity;

	// 총사용포인트
	@Column(nullable = false)
	protected int pointOrderHistoryTotalPrice;

	// 주문상태(구매/환불/교환/취소)
	@Column(length = 20)
	protected String pointOrderHistoryStatus;

	// 생성일
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	protected LocalDateTime pointOrderHistoryCreate;

	// 수정일
	@Column
	protected LocalDateTime pointOrderHistoryUpdate;

	// 회원아이디
	@Column(nullable = false)
	protected String memberId;

	// 상품아이디
	@Column(nullable = false)
	protected Long pointItemId;

	// 기프티콘 발송여부
	@Column(nullable = false, columnDefinition = "char(1) default 'N'")
	protected String pointItemSendCheck;

	// 상품이름
	@Column(nullable = false)
	protected String pointItemName;

	// 생성일 자동 설정
	@PrePersist
	protected void onCreate() {
		this.pointOrderHistoryCreate = LocalDateTime.now();
	}

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointOrderHistoryUpdate = LocalDateTime.now();
	}
}
