package com.mogatshoo.dev.admin.point.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;

public interface AdminPointItemImgRepository extends JpaRepository<AdminPointItemImgEntity, Long> {

	AdminPointItemImgEntity findByPointItemId(long pointItemId);

}
