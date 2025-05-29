package com.mogatshoo.dev.admin.point.item.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/point/item")
public class AdminPointItemController {

	@GetMapping("/list")
	public String pointItemListPage(Model model) {
		
		return "admin/point/item/list";
	}
}
