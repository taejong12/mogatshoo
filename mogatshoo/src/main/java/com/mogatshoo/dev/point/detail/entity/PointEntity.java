package com.mogatshoo.dev.point.detail.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "point")
@Table(name = "point")
@Setter
@Getter
@ToString
public class PointEntity {

	@Id
	private String memberId;

	@Column(columnDefinition = "int default 0")
	private Integer point;

	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp ON UPDATE current_timestamp")
	private LocalDateTime pointUpdate;

}
