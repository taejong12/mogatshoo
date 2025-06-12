package com.mogatshoo.dev.admin.point.item.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface AdminPointItemImgRepository extends JpaRepository<AdminPointItemImgEntity, Long> {

	AdminPointItemImgEntity findByPointItemId(long pointItemId);

	List<AdminPointItemImgEntity> findByPointItemIdIn(List<Long> pointItemIdList);

}
