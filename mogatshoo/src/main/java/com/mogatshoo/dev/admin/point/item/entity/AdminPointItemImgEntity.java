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

@Entity(name = "point_item_img")
@Table(name = "point_item_img")
@Getter
@Setter
@ToString
public class AdminPointItemImgEntity {

	// 포인트 상품 이미지 아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointItemImgId;

	// 이미지 파일 이름
	@Column(nullable = false)
	private String pointItemImgName;

	// 이미지 파일 경로
	@Column(nullable = false)
	private String pointItemImgPath;

	// 등록일
	@Column(insertable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
	private LocalDateTime pointItemImgCreate;

	// 수정일
	@Column(insertable = false, columnDefinition = "timestamp default current_timestamp on update current_timestamp")
	private LocalDateTime pointItemImgUpdate;

	// 상품아이디
	@Column(nullable = false)
	private Long pointItemId;
	
	// 실제 파일 저장 아이디
	@Column(nullable = false)
	private String pointItemImgFileId;

}
