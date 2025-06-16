package com.mogatshoo.dev.admin.point.send.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;

public interface AdminPointItemSendRepository extends JpaRepository<AdminPointItemSendEntity, Long> {

	Page<AdminPointItemSendEntity> findByPointItemSendCheck(String pointItemSendCheck, Pageable pageable);

}
