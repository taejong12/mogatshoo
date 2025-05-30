package com.mogatshoo.dev.admin.point.item.entity;

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

@Entity(name = "point_order_history")
@Table(name = "point_order_history")
@Getter
@Setter
@ToString
public class AdminPointOrderHistoryEntity {

	// 포인트 구매내역 아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointOrderHistoryId;

	// 구매수량
	@Column(nullable = false)
	private int pointOrderHistoryQuantity;

	// 총구매가격
	@Column(nullable = false)
	private int pointOrderHistoryTotalPrice;

	// 예: "구매", "환불", "교환", "취소"
	@Column(nullable = false, length = 20)
	private String pointOrderHistoryStatus;

	// 등록일
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
}
