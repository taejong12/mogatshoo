package com.mogatshoo.dev.point.shop.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mogatshoo.dev.point.shop.entity.PointOrderHistoryEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopCategoryEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopEntity;
import com.mogatshoo.dev.point.shop.service.PointOrderHistoryService;
import com.mogatshoo.dev.point.shop.service.PointShopCategoryService;
import com.mogatshoo.dev.point.shop.service.PointShopService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/point/shop")
public class PointShopController {

	@Autowired
	private PointShopService pointShopService;

	@Autowired
	private PointShopCategoryService pointShopCategoryService;

	@Autowired
	private PointOrderHistoryService pointOrderHistoryService;

	@GetMapping("/list")
	public String pointShopList(@RequestParam(value = "pointCategoryId", required = false) Integer pointCategoryId,
			@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		List<PointShopCategoryEntity> pointCategoryList = pointShopCategoryService.findAll();

		// 페이지당 데이터 수 및 페이지 설정
		int size = 10;
		Pageable pageable = PageRequest.of(page, size, Sort.by("pointItemId").descending());

		Page<PointShopEntity> pointShopPage;

		// 카테고리 필터링 적용 여부
		if (pointCategoryId != null) {
			pointShopPage = pointShopService.findByPointCategoryId(pointCategoryId, pageable);
			// 선택된 카테고리 표시용
			model.addAttribute("selectedCategoryId", pointCategoryId);
		} else {
			pointShopPage = pointShopService.findAll(pageable);
		}

		// 페이지네이션 계산
		int currentPage = pointShopPage.getNumber();
		int totalPages = pointShopPage.getTotalPages();

		int pageBlockSize = 10;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		model.addAttribute("pointCategoryList", pointCategoryList);
		model.addAttribute("pointShopList", pointShopPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);
		return "point/shop/list";
	}

	@GetMapping("/detail/{pointItemId}")
	public String pointShopDetailPage(@PathVariable("pointItemId") Long pointItemId, Model model) {
		PointShopEntity pointShopEntity = pointShopService.findById(pointItemId);
		model.addAttribute("pointShop", pointShopEntity);
		return "point/shop/detail";
	}

	@PostMapping("/buyCheck")
	@ResponseBody
	public Map<String, Object> checkBuyPossiblePointItem(@RequestBody Map<String, Object> request) {
		Map<String, Object> map = new HashMap<>();
		Object idObj = request.get("pointItemId");
		Long pointItemId = Long.valueOf(idObj.toString());
		map = pointShopService.checkBuyPossiblePointItem(pointItemId);
		return map;
	}

	@PostMapping("/buy")
	public String buyPointItem(@RequestParam("pointItemId") Long pointItemId, HttpSession session) {
		// 1. 상품조회 (상품아이디)
		// 2. 사용자포인트조회 (회원아이디)
		// 3. 포인트차감 (회원포인트 - 상품포인트)
		// 4. 포인트내역저장 (상품구매, 상품포인트)
		// 5. 포인상품구매내역저장
		System.out.println("buy() received pointItemId: " + pointItemId);
		pointShopService.buyPointItem(pointItemId);
		session.setAttribute("pointItemId", pointItemId);
		session.setAttribute("pointItemSetTime", System.currentTimeMillis());
		return "redirect:/point/shop/complete";
	}

	@GetMapping("/complete")
	public String completeBuyPointItem(Model model, HttpSession session) {
		Long pointItemId = (Long) session.getAttribute("pointItemId");
		Long setTime = (Long) session.getAttribute("pointItemSetTime");

		System.out.println("complete() session pointItemId: " + pointItemId);
		try {
			if (pointItemId == null)
				return "redirect:/";

			PointShopEntity pointShopEntity = pointShopService.findById(pointItemId);
			model.addAttribute("pointShop", pointShopEntity);
			return "point/shop/complete";
		} finally {
			// 1분 지나면 삭제
			if (setTime != null && System.currentTimeMillis() - setTime > 60000) {
				session.removeAttribute("pointItemId");
				session.removeAttribute("pointItemSetTime");
			}
		}
	}

	@GetMapping("/buyList")
	public String PointItemBuyListPage(@RequestParam(value = "page", defaultValue = "0") int page, Principal principal,
			Model model) {

		// 페이지당 데이터 수 및 페이지 설정
		int size = 12;
		Pageable pageable = PageRequest.of(page, size, Sort.by("pointOrderHistoryId").descending());

		String memberId = principal.getName();

		Page<PointOrderHistoryEntity> pointOrderHistoryPage = pointOrderHistoryService.findByMemberId(memberId,
				pageable);

		pointOrderHistoryPage = pointShopService.findPointItemName(pointOrderHistoryPage);

		// 페이지네이션 계산
		int currentPage = pointOrderHistoryPage.getNumber();
		int totalPages = pointOrderHistoryPage.getTotalPages();

		int pageBlockSize = 10;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		model.addAttribute("pointItemBuyList", pointOrderHistoryPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);
		return "point/shop/buyList";
	}
}
