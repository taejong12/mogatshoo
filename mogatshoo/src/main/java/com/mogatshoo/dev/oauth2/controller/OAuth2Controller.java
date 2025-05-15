package com.mogatshoo.dev.oauth2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mogatshoo.dev.member.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/oauth2")
public class OAuth2Controller {
	
	@Autowired
	private MemberService memberService;
	
	@GetMapping("/join")
	public String oauth2JoinPage(HttpSession session, Model model) {
		OAuth2User oauth2User = (OAuth2User) session.getAttribute("oauth2User");
		
		if(oauth2User == null) {
			return "redirect: /member/login";
		}
		
		String provider = oauth2User.getAttribute("provider");
		String providerId = oauth2User.getName();
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		String birth = oauth2User.getAttribute("birth");
		String mobile = oauth2User.getAttribute("mobile");
		String gender = oauth2User.getAttribute("gender");
		String memberId = provider+"_"+providerId;
		
		if(mobile != null) {
			mobile = mobile.replace("-", "");
		}
		
		if(gender != null) {
			if(gender.equals("M")) {
				gender = "남";
			}else {
				gender = "여";
			}
		}
		
		if(email != null) {
			Boolean emailCheck = memberService.memberEmailCheck(oauth2User.getAttribute("email"));
			
			if(!emailCheck) {
				return "oauth2/error";
			}
		}
		
		model.addAttribute("provider", provider);
		model.addAttribute("providerId", providerId);
		model.addAttribute("email", email);
		model.addAttribute("name", name);
		model.addAttribute("birth", birth);
		model.addAttribute("mobile", mobile);
		model.addAttribute("gender", gender);
		model.addAttribute("memberId", memberId);
		return "member/join";
	}
}