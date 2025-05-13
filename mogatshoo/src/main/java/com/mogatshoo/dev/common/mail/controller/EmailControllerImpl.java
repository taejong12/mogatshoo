package com.mogatshoo.dev.common.mail.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.mogatshoo.dev.common.mail.service.EmailService;

import jakarta.servlet.http.HttpSession;

@RestController
public class EmailControllerImpl implements EmailController{

	@Autowired
	private EmailService mailService;
	
	@Override
	public Map<String, Object> sendAuthEmail(String memberEmail, HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		String title = "[SpringBoot] 인증번호 전송";
		String authCode = UUID.randomUUID().toString().substring(0, 6);
		
		String html = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>";
		html += "<h2 style='color: #333333;'>Spring Boot 인증번호 안내</h2>";
		html += "<p style='font-size: 16px; color: #555555;'>아래 인증번호를 입력하여 본인 확인을 완료해주세요.</p>";
		html += "<div style='margin: 30px 0; text-align: center;'>";
		html += "<span style='display: inline-block; padding: 15px 25px; font-size: 24px; border-radius: 6px; border: 1px solid black;'>" + authCode + "</span>";
		html += "</div>";
		html += "<p style='font-size: 14px; color: #999999;'>본 이메일은 발신 전용입니다. 문의가 필요하신 경우 웹사이트를 통해 연락해주세요.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>감사합니다.<br>SpringBoot 드림</p>";
		html += "</div>";
		html += "</body></html>";
		
		mailService.sendAuthEmail(title, memberEmail, html);
		
		String msg = "메일이 전송 되었습니다.";
		map.put("result", true);
		map.put("msg", msg);
		
		session.setAttribute("authCode", authCode);
		return map;
	}

	@Override
	public Map<String, Object> authCodeConfirm(String memberAuthCode, HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		String authCode = (String) session.getAttribute("authCode");
		String msg = null;
		
		if(authCode != null && authCode.equals(memberAuthCode)) {
			session.removeAttribute("authCode");
			msg = "인증 성공";
			map.put("result", true);
			map.put("msg", msg);
			return map;
		} else {
			msg = "인증 실패";
			map.put("result", false);
			map.put("msg", msg);
			return map;
		}
	}
	

	
}
