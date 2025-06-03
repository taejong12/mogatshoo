package com.mogatshoo.dev.admin.point.category.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mogatshoo.dev.admin.point.category.entity.AdminPointCategoryEntity;
import com.mogatshoo.dev.admin.point.category.service.AdminPointCategoryService;
import com.mogatshoo.dev.admin.point.item.service.AdminPointItemService;

@Controller
@RequestMapping("/admin/point/category")
public class AdminPointCategoryController {

	@Autowired
	private AdminPointCategoryService adminPointCategoryService;
	
	@Autowired
	private AdminPointItemService adminPointItemService;

	@GetMapping("/list")
	public String adminPointCategoryListPage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		// 페이지당 데이터 수 및 페이지 설정
		int size = 10;
		Pageable pageable = PageRequest.of(page, size,
				Sort.by(Sort.Order.asc("pointCategorySortOrder"), Sort.Order.desc("pointCategoryUpdate")));

		Page<AdminPointCategoryEntity> pointCategoryPage = adminPointCategoryService.findAllPageable(pageable);

		// 페이지네이션 계산
		int currentPage = pointCategoryPage.getNumber();
		int totalPages = pointCategoryPage.getTotalPages();

		int pageBlockSize = 10;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		model.addAttribute("pointCategoryList", pointCategoryPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);
		return "admin/point/category/list";
	}

	@GetMapping("/insert")
	public String adminPointCategoryInsertPage() {
		return "admin/point/category/insertPage";
	}

	@PostMapping("/insert")
	public String adminPointCategoryInsert(@ModelAttribute AdminPointCategoryEntity adminPointCategoryEntity) {
		adminPointCategoryService.save(adminPointCategoryEntity);
		return "redirect:/admin/point/category/list";
	}

	@GetMapping("/detail/{pointCategoryId}")
	public String pointCategoryDetailPage(@PathVariable("pointCategoryId") Integer pointCategoryId, Model model) {
		AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryService.findById(pointCategoryId);
		model.addAttribute("pointCategory", adminPointCategoryEntity);
		return "admin/point/category/detail";
	}

	@GetMapping("/update/{pointCategoryId}")
	public String pointCategoryUpdatePage(@PathVariable("pointCategoryId") Integer pointCategoryId, Model model) {
		AdminPointCategoryEntity adminPointCategoryEntity = adminPointCategoryService.findById(pointCategoryId);
		model.addAttribute("pointCategory", adminPointCategoryEntity);
		return "admin/point/category/update";
	}

	@PostMapping("/deleteCheck")
	@ResponseBody
	Map<String, Object> pointCategoryDeleteCheck(@RequestBody Map<String, Object> request){
		Map<String, Object> map = new HashMap<>();
		Integer pointCategoryId = Integer.parseInt((String) request.get("pointCategoryId"));
		map = adminPointItemService.deletePointCategoryCheck(pointCategoryId);
		return map;
	}
	
	@PostMapping("/delete")
	public String pointCategoryDelete(@RequestParam("pointCategoryId") Integer pointCategoryId) {
		adminPointCategoryService.deletePointCategory(pointCategoryId);
		return "redirect:/admin/point/category/list";
	}

	@PostMapping("/update")
	public String pointCategoryUpdate(@ModelAttribute AdminPointCategoryEntity adminPointCategoryEntity) {
		adminPointCategoryService.updatePointCategory(adminPointCategoryEntity);
		return "redirect:/admin/point/category/detail/" + adminPointCategoryEntity.getPointCategoryId();
	}
}
