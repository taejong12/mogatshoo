package com.mogatshoo.dev.admin.point.item.controller;

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

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;
import com.mogatshoo.dev.admin.point.category.service.AdminPointCategoryService;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemImgService;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemService;

@Controller
@RequestMapping("/admin/point/item")
public class AdminPointItemController {

	@Autowired
	private AdminPointItemService adminPointItemService;

	@Autowired
	private AdminPointItemImgService adminPointItemImgService;

	@Autowired
	private AdminPointCategoryService adminPointCategoryService;

	@GetMapping("/list")
	public String pointItemListPage(@RequestParam(value = "pointCategoryId", required = false) Integer pointCategoryId,
			@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		List<AdminPointCategoryEntity> pointCategoryList = adminPointCategoryService.findAll();
		model.addAttribute("pointCategoryList", pointCategoryList);

		// 페이지당 데이터 수 및 페이지 설정
		int size = 10;
		Pageable pageable = PageRequest.of(page, size, Sort.by("pointItemId").descending());

		Page<AdminPointItemEntity> pointItemPage;

		// 카테고리 필터링 적용 여부
		if (pointCategoryId != null) {
			pointItemPage = adminPointItemService.findByPointCategoryId(pointCategoryId, pageable);
			// 선택된 카테고리 표시용
			model.addAttribute("selectedCategoryId", pointCategoryId);
		} else {
			pointItemPage = adminPointItemService.findAll(pageable);
		}

		// 페이지네이션 계산
		int currentPage = pointItemPage.getNumber();
		int totalPages = pointItemPage.getTotalPages();

		int pageBlockSize = 10;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		// 상품이미지 조회
		List<AdminPointItemImgEntity> pointItemImgList = adminPointItemImgService
				.findByItemId(pointItemPage.getContent());

		model.addAttribute("pointItemList", pointItemPage.getContent());
		model.addAttribute("pointItemImgList", pointItemImgList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);

		return "admin/point/item/list";
	}

	@GetMapping("/insert")
	public String pointItemInsertPage() {

		return "admin/point/item/insertItem";
	}
}
