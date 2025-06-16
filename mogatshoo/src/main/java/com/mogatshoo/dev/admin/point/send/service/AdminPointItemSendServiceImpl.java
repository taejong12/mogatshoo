package com.mogatshoo.dev.admin.point.send.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;
import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;
import com.mogatshoo.dev.admin.point.send.repository.AdminPointItemSendRepository;
import com.mogatshoo.dev.admin.point.send.repository.PointItemSendLogRepository;
import com.mogatshoo.dev.common.authentication.CommonUserName;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminPointItemSendServiceImpl implements AdminPointItemSendService {

	private static final Logger logger = LoggerFactory.getLogger(AdminPointItemSendServiceImpl.class);

	@Autowired
	private AdminPointItemSendRepository adminPointItemSendRepository;

	@Autowired
	private PointItemSendLogRepository pointItemSendLogRepository;
	
	@Autowired
	private CommonUserName commonUserName;

	@Override
	public Page<AdminPointItemSendEntity> findAll(Pageable pageable) {
		try {
			Page<AdminPointItemSendEntity> page = adminPointItemSendRepository.findAll(pageable);
			logger.info("포인트상품 구매내역 전체 조회 성공: 총 {}건", page.getTotalElements());
			return page;
		} catch (Exception e) {
			logger.error("포인트상품 구매내역 전체 조회 중 오류 발생", e);
			return Page.empty(pageable); // 페이지 정보 포함해 반환
		}
	}

	@Override
	public Page<AdminPointItemSendEntity> findByPointItemSendCheck(String pointItemSendCheck, Pageable pageable) {
		try {
			Page<AdminPointItemSendEntity> page = adminPointItemSendRepository
					.findByPointItemSendCheck(pointItemSendCheck, pageable);
			logger.info("발송상태 '{}'에 대한 구매내역 조회 성공: {}건", pointItemSendCheck, page.getTotalElements());
			return page;
		} catch (Exception e) {
			logger.error("발송상태 '{}' 구매내역 조회 중 오류 발생", pointItemSendCheck, e);
			return Page.empty(pageable); // 빈 페이지 반환 (페이지 정보 유지)
		}
	}

	@Override
	public AdminPointItemSendEntity findById(Long pointOrderHistoryId) {
		try {
			return adminPointItemSendRepository.findById(pointOrderHistoryId)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구매내역 ID: " + pointOrderHistoryId));
		} catch (Exception e) {
			logger.error("구매내역 ID {}에 대한 상세조회 중 오류 발생", pointOrderHistoryId, e);
			throw e;
		}
	}

	@Override
	public void updatePointItemSendCheck(Long historyId) {
		try {
			AdminPointItemSendEntity updatePointItemSendCheckEntity = adminPointItemSendRepository.findById(historyId)
					.orElseThrow(() -> new IllegalArgumentException("해당 ID의 주문 내역이 존재하지 않습니다. ID: " + historyId));

			updatePointItemSendCheckEntity.setPointItemSendCheck("Y");

			logger.info("pointItemSendCheck 상태를 'Y'로 성공적으로 변경 - 주문 ID: {}", historyId);
		} catch (Exception e) {
			logger.error("pointItemSendCheck 업데이트 중 오류 발생 - 주문 ID: {}", historyId, e);
			throw e;
		}
	}

	@Override
	public void saveSendLog(PointItemSendLogEntity pointItemSendLogEntity) {
		try {
			String adminId = commonUserName.getUserName();
			pointItemSendLogEntity.setAdminId(adminId);
			pointItemSendLogRepository.save(pointItemSendLogEntity);
			logger.info("기프티콘 발송 로그 저장 성공 - 주문 ID: {}, 수신자 이메일: {}", pointItemSendLogEntity.getPointOrderHistoryId(),
					pointItemSendLogEntity.getMemberEmail());
		} catch (Exception e) {
			logger.error("기프티콘 발송 로그 저장 중 오류 발생 - 주문 ID: {}, 수신자 이메일: {}",
					pointItemSendLogEntity.getPointOrderHistoryId(), pointItemSendLogEntity.getMemberEmail(), e);
			throw new RuntimeException("기프티콘 발송 로그 저장 중 오류가 발생했습니다.", e);
		}
	}
}
