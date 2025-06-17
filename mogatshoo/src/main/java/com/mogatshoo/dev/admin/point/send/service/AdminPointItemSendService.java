package com.mogatshoo.dev.admin.point.send.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;
import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;

public interface AdminPointItemSendService {

	Page<AdminPointItemSendEntity> findAll(Pageable pageable);

	Page<AdminPointItemSendEntity> findByPointItemSendCheck(String pointItemSendCheck, Pageable pageable);

	AdminPointItemSendEntity findById(Long pointOrderHistoryId);

	void updatePointItemSendCheck(Long historyId);

	void saveSendLog(PointItemSendLogEntity pointItemSendLogEntity);

	PointItemSendLogEntity findBySendLogId(Long logId);

	AdminPointItemSendEntity findByHistoryId(Long historyId);

}
