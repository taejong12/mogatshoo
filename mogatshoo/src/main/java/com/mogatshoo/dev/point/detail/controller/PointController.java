package com.mogatshoo.dev.point.detail.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mogatshoo.dev.point.detail.service.PointHistoryService;
import com.mogatshoo.dev.point.detail.entity.PointHistoryEntity;

@Controller
@RequestMapping("/point/detail")
public class PointController {

	@Autowired
	private PointHistoryService pointHistoryService;

	@GetMapping("/list")
	public String memberPointList(@RequestParam("memberId") String memberId,
			@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		// 페이지당 데이터 수
		int size = 12;
		Pageable pageable = PageRequest.of(page, size, Sort.by("pointHistoryId").descending());
		Page<PointHistoryEntity> pointHistoryPage = pointHistoryService.findByMemberId(memberId, pageable);

		int currentPage = pointHistoryPage.getNumber();
		int totalPages = pointHistoryPage.getTotalPages();

		int pageBlockSize = 12;
		int startPage = (currentPage / pageBlockSize) * pageBlockSize;
		int endPage = Math.min(startPage + pageBlockSize, totalPages);

		model.addAttribute("pointHistoryPage", pointHistoryPage);
		model.addAttribute("pointHistoryList", pointHistoryPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("memberId", memberId);
		return "point/detail/list";
	}
}
