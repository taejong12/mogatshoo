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
@Table(name = "point_order_history")
@Getter
@Setter
@ToString
public class PointOrderHistoryEntity {

	// 구매내역아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointOrderHistoryId;

	// 구매수량
	@Column(nullable = false)
	private int pointOrderHistoryQuantity;

	// 총사용포인트
	@Column(nullable = false)
	private int pointOrderHistoryTotalPrice;

	// 주문상태(구매/환불/교환/취소)
	@Column(length = 20)
	private String pointOrderHistoryStatus;

	// 생성일
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime pointOrderHistoryCreate;

	// 수정일
	@Column(insertable = false, columnDefinition = "timestamp default current_timestamp on update current_timestamp")
	private LocalDateTime pointOrderHistoryUpdate;

	// 회원아이디
	@Column(nullable = false)
	private String memberId;

	// 상품아이디
	@Column(nullable = false)
	private Long pointItemId;

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
