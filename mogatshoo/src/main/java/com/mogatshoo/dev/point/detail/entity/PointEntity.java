package com.mogatshoo.dev.point.detail.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "point")
@Setter
@Getter
@ToString
public class PointEntity {

	@Id
	private String memberId;

	@Column(columnDefinition = "int default 0")
	private Integer point;

	// 수정일
	@Column(columnDefinition = "DATETIME")
	private LocalDateTime pointUpdate;

	// 수정일 자동 설정
	@PreUpdate
	protected void onUpdate() {
		this.pointUpdate = LocalDateTime.now();
	}

	@Version
	private Integer version;
}
