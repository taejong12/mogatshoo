package com.mogatshoo.dev.admin.point.send.controller;

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
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;
import com.mogatshoo.dev.admin.point.send.service.AdminPointItemSendService;

@Controller
@RequestMapping("/admin/point/send")
public class AdminPointItemSendController {

	@Autowired
	private AdminPointItemSendService adminPointItemSendService;

	@GetMapping("/list")
	public String PointItemSendListPage(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "sendStatus", required = false) String sendStatus, Model model) {

		// 페이지당 데이터 수 및 페이지 설정
		int size = 10;
		Pageable pageable = PageRequest.of(page, size, Sort.by("pointOrderHistoryId").descending());

		Page<AdminPointItemSendEntity> adminPointItemSendPage;

		if ("Y".equals(sendStatus)) {
			adminPointItemSendPage = adminPointItemSendService.findByPointItemSendCheck("Y", pageable);
		} else if ("N".equals(sendStatus)) {
			adminPointItemSendPage = adminPointItemSendService.findByPointItemSendCheck("N", pageable);
		} else {
			adminPointItemSendPage = adminPointItemSendService.findAll(pageable);
		}

		// 페이지네이션 계산
		int currentPage = adminPointItemSendPage.getNumber();
		int totalPages = adminPointItemSendPage.getTotalPages();

		int pageBlockSize = 10;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		model.addAttribute("sendStatus", sendStatus);
		model.addAttribute("pointOrderHistoryList", adminPointItemSendPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);

		return "admin/point/send/list";
	}
}
