package com.mogatshoo.dev.point.shop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mogatshoo.dev.point.shop.entity.PointShopCategoryEntity;
import com.mogatshoo.dev.point.shop.entity.PointShopEntity;
import com.mogatshoo.dev.point.shop.service.PointShopCategoryService;
import com.mogatshoo.dev.point.shop.service.PointShopService;

@Controller
@RequestMapping("/point/shop")
public class PointShopController {

	@Autowired
	private PointShopService pointShopService;

	@Autowired
	private PointShopCategoryService pointShopCategoryService;

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
}
