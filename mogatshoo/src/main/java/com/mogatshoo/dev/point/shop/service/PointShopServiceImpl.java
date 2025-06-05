package com.mogatshoo.dev.point.shop.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.common.authentication.CommonUserName;
import com.mogatshoo.dev.point.detail.entity.PointEntity;
import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;
import com.mogatshoo.dev.point.detail.service.PointHistoryService;
import com.mogatshoo.dev.point.detail.service.PointService;
import com.mogatshoo.dev.point.shop.entity.PointOrderHistoryEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopCategoryEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopImgEntity;
import com.mogatshoo.dev.point.shop.repository.PointShopRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PointShopServiceImpl implements PointShopService {

	private static final Logger logger = LoggerFactory.getLogger(PointShopServiceImpl.class);

	@Autowired
	private PointShopRepository pointShopRepository;

	@Autowired
	private PointShopImgService pointShopImgService;

	@Autowired
	private PointShopCategoryService pointShopCategoryService;

	@Autowired
	private PointService pointService;

	@Autowired
	private PointHistoryService pointHistoryService;

	@Autowired
	private PointOrderHistoryService pointOrderHistoryService;

	@Autowired
	private CommonUserName commonUserName;

	@Override
	public Page<PointShopEntity> findByPointCategoryId(Integer pointCategoryId, Pageable pageable) {
		try {
			Page<PointShopEntity> page = pointShopRepository.findByPointCategoryId(pointCategoryId, pageable);

			page.forEach(shop -> {
				PointShopImgEntity imgFile = pointShopImgService.findByPointItemId(shop.getPointItemId());
				shop.setImgFile(imgFile);
			});

			return page;
		} catch (Exception e) {
			logger.error("포인트 카테고리 ID로 포인트샵 항목 조회 중 오류 발생. categoryId = {}", pointCategoryId, e);
			return Page.empty();
		}
	}

	@Override
	public Page<PointShopEntity> findAll(Pageable pageable) {
		try {
			Page<PointShopEntity> page = pointShopRepository.findAll(pageable);

			page.forEach(shop -> {
				PointShopImgEntity imgFile = pointShopImgService.findByPointItemId(shop.getPointItemId());
				shop.setImgFile(imgFile);
			});

			return page;
		} catch (Exception e) {
			logger.error("포인트샵 전체 목록 조회 중 오류 발생", e);
			return Page.empty();
		}
	}

	@Override
	public PointShopEntity findById(Long pointItemId) {
		try {
			PointShopEntity pointShopEntity = pointShopRepository.findById(pointItemId).orElse(null);

			if (pointShopEntity == null) {
				logger.warn("포인트 아이템을 찾을 수 없습니다. ID: {}", pointItemId);
				return null;
			}

			PointShopImgEntity pointShopImgEntity = pointShopImgService.findByPointItemId(pointItemId);
			PointShopCategoryEntity pointShopCategoryEntity = pointShopCategoryService
					.findById(pointShopEntity.getPointCategoryId());

			pointShopEntity.setImgFile(pointShopImgEntity);
			pointShopEntity.setCategory(pointShopCategoryEntity);

			logger.info("포인트 아이템 조회 완료 - ID: {}", pointItemId);

			return pointShopEntity;

		} catch (Exception e) {
			logger.error("포인트 아이템 조회 중 오류 발생. ID: {}", pointItemId, e);
			throw e;
		}
	}

	@Override
	public Map<String, Object> checkBuyPossiblePointItem(Long pointItemId) {
		Map<String, Object> map = new HashMap<>();

		try {
			String memberId = commonUserName.getUserName();
			PointEntity pointEntity = pointService.findById(memberId);

			if (pointEntity == null) {
				logger.warn("포인트 정보를 찾을 수 없습니다. memberId: {}", memberId);
				map.put("buyCheck", false);
				map.put("msg", "포인트 정보를 찾을 수 없습니다.");
				return map;
			}

			PointShopEntity pointShopEntity = pointShopRepository.findById(pointItemId).orElse(null);

			if (pointShopEntity == null) {
				logger.warn("상품 정보를 찾을 수 없습니다. pointItemId: {}", pointItemId);
				map.put("buyCheck", false);
				map.put("msg", "상품 정보를 찾을 수 없습니다.");
				return map;
			}

			boolean pointCheck = pointEntity.getPoint() >= pointShopEntity.getPointItemPrice();
			boolean stockCheck = pointShopEntity.getPointItemStock() > 0;

			if (!pointCheck) {
				map.put("msg", "포인트가 부족합니다.");
			}

			if (!stockCheck) {
				map.put("msg", map.containsKey("msg") ? map.get("msg") + " 재고가 부족합니다." : "재고가 부족합니다.");
			}

			map.put("buyCheck", pointCheck && stockCheck);
			return map;

		} catch (Exception e) {
			logger.error("포인트 아이템 구매 가능 여부 확인 중 오류 발생 - pointItemId: {}", pointItemId, e);
			map.put("buyCheck", false);
			map.put("msg", "내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			return map;
		}
	}

	@Override
	public void buyPointItem(Long pointItemId) {

		try {
			int quantity = 1;

			// 1. 상품 조회
			PointShopEntity pointItem = pointShopRepository.findByIdForUpdate(pointItemId);
			if (pointItem == null) {
				logger.warn("상품을 찾을 수 없습니다. pointItemId: {}", pointItemId);
				throw new IllegalArgumentException("상품이 존재하지 않습니다.");
			}
			long totalPrice = pointItem.getPointItemPrice() * quantity;

			// 2. 사용자 포인트 조회
			String memberId = commonUserName.getUserName();
			PointEntity pointEntity = pointService.findById(memberId);
			if (pointEntity == null) {
				logger.warn("회원 포인트 정보가 존재하지 않습니다. memberId: {}", memberId);
				throw new IllegalStateException("회원 정보 오류");
			}

			// 3. 포인트 차감
			if (pointEntity.getPoint() < totalPrice) {
				logger.info("포인트 부족 - memberId: {}, 보유 포인트: {}, 필요 포인트: {}", memberId, pointEntity.getPoint(),
						totalPrice);
				throw new IllegalStateException("포인트가 부족합니다.");
			}

			pointEntity.setPoint(pointEntity.getPoint() - (int) totalPrice);
			logger.info("포인트 차감 완료 - memberId: {}, 차감 포인트: {}", memberId, totalPrice);

			// 4. 포인트 내역 저장
			PointHistoryEntity pointHistory = new PointHistoryEntity();
			pointHistory.setChangePoint((int) totalPrice);
			pointHistory.setReason("포인트샵");
			pointHistory.setType("사용");
			pointHistory.setMemberId(memberId);
			pointHistoryService.pointHistorySave(pointHistory);
			logger.info("포인트 내역 저장 완료 - memberId: {}", memberId);

			// 5. 구매 내역 저장
			PointOrderHistoryEntity pointOrderHistory = new PointOrderHistoryEntity();
			pointOrderHistory.setPointOrderHistoryQuantity(quantity);
			pointOrderHistory.setPointOrderHistoryTotalPrice((int) totalPrice);
			pointOrderHistory.setPointOrderHistoryStatus("구매");
			pointOrderHistory.setMemberId(memberId);
			pointOrderHistory.setPointItemId(pointItemId);
			pointOrderHistory.setPointItemSendCheck("N");
			pointOrderHistoryService.pointOrderHistorySave(pointOrderHistory);
			logger.info("구매 내역 저장 완료 - memberId: {}, pointItemId: {}", memberId, pointItemId);

			int currentStock = pointItem.getPointItemStock();

			// 6. 재고 차감 전에 재고 확인
			if (currentStock < quantity) {
				throw new IllegalStateException("재고 부족 - 요청 수량: " + quantity + ", 현재 재고: " + currentStock);
			}

			// 재고 차감
			pointItem.setPointItemStock(currentStock - quantity);
			logger.info("재고 차감 완료 - pointItemId: {}, 차감 수량: {}", pointItemId, quantity);

			// 7. 재고가 0이면 판매 중단
			if (pointItem.getPointItemStock() == 0) {
				pointItem.setPointItemSaleStatus("N");
				logger.info("재고 소진으로 판매 중단 - pointItemId: {}", pointItemId);
			}
		} catch (Exception e) {
			logger.error("포인트 상품 구매 처리 중 오류 발생 - pointItemId: {}", pointItemId, e);
			throw e;
		}
	}

	@Override
	public Page<PointOrderHistoryEntity> findPointItemName(Page<PointOrderHistoryEntity> pointOrderHistoryPage) {
		pointOrderHistoryPage.forEach(history -> {
			try {
				PointShopEntity pointItem = pointShopRepository.findById(history.getPointItemId()).orElseThrow(
						() -> new IllegalArgumentException("해당 포인트상품이 없습니다. ID: " + history.getPointItemId()));
				history.setPointShop(pointItem);
			} catch (Exception e) {
				logger.error("포인트 상품 조회 중 오류 발생 - pointItemId: {}", history.getPointItemId(), e);
			}
		});

		return pointOrderHistoryPage;
	}
}
