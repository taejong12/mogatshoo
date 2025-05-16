package com.mogatshoo.dev.point.entity;

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

@Entity(name = "point_history")
@Table(name = "point_history")
@Setter
@Getter
@ToString
public class PointHistoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer pointHistoryId;

	@Column(nullable = false)
	private Integer changePoint;

	// 적립내용
	@Column(nullable = false)
	private String reason;

	// 적립, 사용, 취소 등 (공통된 이름)
	@Column(nullable = false)
	private String type;

	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime pointCreate;

	@Column(nullable = false)
	private String memberId;
}
