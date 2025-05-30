package com.mogatshoo.dev.admin.point.category.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/point/category")
public class AdminPointCategoryController {

	@GetMapping("/list")
	public String adminPointCategoryListPage() {
		return "admin/point/category/list";
	}
}
