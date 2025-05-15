package com.mogatshoo.dev.common.mail.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.mogatshoo.dev.common.mail.service.EmailService;
import com.mogatshoo.dev.member.entity.MemberEntity;

import jakarta.servlet.http.HttpSession;

@RestController
public class EmailControllerImpl implements EmailController{

	@Autowired
	private EmailService mailService;
	
	@Override
	public Map<String, Object> sendAuthEmail(String memberEmail, HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		String title = "[mogatshoo] 인증번호 전송";
		String authCode = UUID.randomUUID().toString().substring(0, 6);
		
		String html = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>";
		html += "<h2 style='color: #333333;'>mogatshoo 인증번호 안내</h2>";
		html += "<p style='font-size: 16px; color: #555555;'>아래 인증번호를 입력하여 본인 확인을 완료해주세요.</p>";
		html += "<div style='margin: 30px 0; text-align: center;'>";
		html += "<span style='display: inline-block; padding: 15px 25px; font-size: 24px; border-radius: 6px; border: 1px solid black;'>" + authCode + "</span>";
		html += "</div>";
		html += "<p style='font-size: 14px; color: #999999;'>본 이메일은 발신 전용입니다. 문의가 필요하신 경우 웹사이트를 통해 연락해주세요.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>감사합니다.<br>mogatshoo 드림</p>";
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

	@Override
	public void findByIdSendEmail(MemberEntity memberEntity) {
		
		String id = memberEntity.getMemberId();
		String title = "[mogatshoo] 회원님의 아이디를 안내드립니다";
		
		String html = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>";
		html += "<h2 style='color: #333333;'>아이디 찾기 요청 결과 안내</h2>";
		html += "<p style='font-size: 16px; color: #555555;'>안녕하세요, mogatshoo입니다.<br><br>회원님께서 요청하신 아이디 찾기 결과를 아래와 같이 안내드립니다.</p>";
		html += "<div style='margin: 30px 0; text-align: center;'>";
		html += "<span style='display: inline-block; padding: 15px 25px; font-size: 24px; border-radius: 6px; border: 1px solid #333333; background-color: #f1f1f1;'>" + id + "</span>";
		html += "</div>";
		html += "<p style='font-size: 14px; color: #555555;'>회원님의 개인정보 보호를 위해 본 이메일은 요청하신 경우에만 발송됩니다.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>본 메일은 발신 전용 메일입니다. 문의 사항이 있으실 경우 웹사이트의 고객센터를 이용해주세요.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>감사합니다.<br><strong>mogatshoo 드림</strong></p>";
		html += "</div>";
		html += "</body></html>";
		
		mailService.findByIdSendEmail(title, memberEntity.getMemberEmail(), html);
		
		
	}
	

	
}
