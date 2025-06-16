package com.mogatshoo.dev.admin.point.send.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mogatshoo.dev.admin.point.send.entity.AdminPointItemSendEntity;
import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;
import com.mogatshoo.dev.admin.point.send.service.AdminPointItemSendService;
import com.mogatshoo.dev.common.mail.controller.EmailController;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;

@Controller
@RequestMapping("/admin/point/send")
public class AdminPointItemSendController {

	@Autowired
	private AdminPointItemSendService adminPointItemSendService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private EmailController emailController;
	
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

	@GetMapping("/item/{pointOrderHistoryId}")
	public String sendItemPage(@PathVariable("pointOrderHistoryId") Long pointOrderHistoryId, Model model) {
		// 1.구매내역 조회
		AdminPointItemSendEntity historyEntity = adminPointItemSendService.findById(pointOrderHistoryId);

		// 2.회원 정보 조회
		String memberId = historyEntity.getMemberId();
		MemberEntity memberEntity = memberService.findByMemberId(memberId);

		model.addAttribute("history", historyEntity);
		model.addAttribute("member", memberEntity);

		return "admin/point/send/item";
	}

	@PostMapping("/item")
	public String sendItem(@ModelAttribute PointItemSendLogEntity pointItemSendLogEntity) {
		
		System.out.println("============================pointItemSendLogEntity");
		System.out.println(pointItemSendLogEntity);
		// 1. 스토리지 이미지 저장
		
	
		// 2. 이메일 전송
		//emailController.sendGiftImg();
		
		// 3. DB 저장 (발송 여부 N -> Y 변경, 발송 로그 저장)
		
		return "redirect:/admin/point/send/complete";
	}

	@GetMapping("/complete")
	public String completePage() {
		return "admin/point/send/complete";
	}

}
