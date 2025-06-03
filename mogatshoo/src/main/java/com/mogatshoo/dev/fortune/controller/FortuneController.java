package com.mogatshoo.dev.fortune.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mogatshoo.dev.fortune.service.FortuneService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.point.detail.service.PointService;

@Controller
@RequestMapping("/fortune")
public class FortuneController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private FortuneService fortuneService;

	@Autowired
	private PointService pointService;

	@GetMapping("/start")
	public String fortuneStartPage(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String memberId = authentication.getName();
		int point = pointService.findByMemberId(memberId);
		model.addAttribute("point", point);
		return "fortune/start";
	}

	@PostMapping("/start")
	@ResponseBody
	public Map<String, Object> fortuneStart() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String memberId = authentication.getName();
		MemberEntity member = memberService.findByMemberId(memberId);

		String name = member.getMemberName();
		LocalDate birth = member.getMemberBirth();
		String gender = member.getMemberGender();

		Map<String, Object> map = new HashMap<>();
		map = fortuneService.fortuneMsg(memberId, name, birth, gender);

		return map;
	}
}
