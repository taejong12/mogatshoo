package com.mogatshoo.dev.fortune.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mogatshoo.dev.fortune.service.FortuneService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;

@Controller
@RequestMapping("/fortune")
public class FortuneController {

	@Autowired
	private MemberService memberService;
	
	@Autowired
	private FortuneService fortuneService;
	
	@GetMapping("/start")
	public String fortuneStartPage() {
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
        map = fortuneService.fortuneMsg(name, birth, gender);
		
		return map;
	}
}
