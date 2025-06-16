package com.mogatshoo.dev.admin.point.send.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;

public interface AdminPointItemSendService {

	Page<AdminPointItemSendEntity> findAll(Pageable pageable);

	Page<AdminPointItemSendEntity> findByPointItemSendCheck(String pointItemSendCheck, Pageable pageable);

}
