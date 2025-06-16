package com.mogatshoo.dev.common.mail.controller;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;

import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;
import com.mogatshoo.dev.common.mail.service.EmailService;
import com.mogatshoo.dev.member.entity.MemberEntity;

import jakarta.servlet.http.HttpSession;

@RestController
public class EmailControllerImpl implements EmailController {

	private static final Logger logger = LoggerFactory.getLogger(EmailControllerImpl.class);

	@Autowired
	private EmailService mailService;

	@Override
	public Map<String, Object> sendAuthEmail(String memberEmail, HttpSession session) {

		session.removeAttribute("authTryCount");

		Map<String, Object> map = new HashMap<String, Object>();
		String title = "[mogatshoo] 인증번호 전송";
		String authCode = UUID.randomUUID().toString().substring(0, 6);

		String html = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>";
		html += "<h2 style='color: #333333;'>mogatshoo 인증번호 안내</h2>";
		html += "<p style='font-size: 16px; color: #555555;'>아래 인증번호를 입력하여 본인 확인을 완료해주세요.</p>";
		html += "<div style='margin: 30px 0; text-align: center;'>";
		html += "<span style='display: inline-block; padding: 15px 25px; font-size: 24px; border-radius: 6px; border: 1px solid black;'>"
				+ authCode + "</span>";
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
		Integer authTryCount = (Integer) session.getAttribute("authTryCount");
		String authCode = (String) session.getAttribute("authCode");
		String msg = null;

		logger.info("authTryCount: {}", authTryCount);

		if (authTryCount == null)
			authTryCount = 0;

		if (authCode != null && authCode.equals(memberAuthCode)) {
			session.removeAttribute("authTryCount");
			session.removeAttribute("authCode");
			session.setAttribute("authSuccess", "authEmail");
			msg = "인증 성공";
			map.put("result", true);
			map.put("msg", msg);
			return map;
		} else {

			authTryCount++;

			if (authTryCount >= 5) {
				msg = "인증 시도 횟수를 초과했습니다. <br> 이메일을 다시 인증해주세요.";
			} else {
				msg = "인증 실패 (" + authTryCount + " / 5)";
				session.setAttribute("authTryCount", authTryCount);
			}

			map.put("authTryCount", authTryCount);
			map.put("result", false);
			map.put("msg", msg);
			return map;
		}
	}

	@Async
	@Override
	public void findByIdSendEmail(MemberEntity memberEntity) {

		String id = memberEntity.getMemberId();
		String title = "[mogatshoo] 회원님의 아이디를 안내드립니다";

		String html = "<html><body style='font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'>";
		html += "<h2 style='color: #333333;'>아이디 찾기 요청 결과 안내</h2>";
		html += "<p style='font-size: 16px; color: #555555;'>안녕하세요, mogatshoo입니다.<br><br>회원님께서 요청하신 아이디 찾기 결과를 아래와 같이 안내드립니다.</p>";
		html += "<div style='margin: 30px 0; text-align: center;'>";
		html += "<span style='display: inline-block; padding: 15px 25px; font-size: 24px; border-radius: 6px; border: 1px solid #333333; background-color: #f1f1f1;'>"
				+ id + "</span>";
		html += "</div>";
		html += "<p style='font-size: 14px; color: #555555;'>회원님의 개인정보 보호를 위해 본 이메일은 요청하신 경우에만 발송됩니다.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>본 메일은 발신 전용 메일입니다. 문의 사항이 있으실 경우 웹사이트의 고객센터를 이용해주세요.</p>";
		html += "<p style='font-size: 14px; color: #999999;'>감사합니다.<br><strong>mogatshoo 드림</strong></p>";
		html += "</div>";
		html += "</body></html>";

		mailService.findByIdSendEmail(title, memberEntity.getMemberEmail(), html);
	}

	@Override
	public void sendGiftImg(PointItemSendLogEntity pointItemSendLogEntity) {

		String title = "🎁 [mogatshoo] 기프티콘 발송 완료 안내";
		String imgUrl = pointItemSendLogEntity.getImgURL();

		String html = "<!DOCTYPE html>";
		html += "<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;'>";
		html += "<div style='max-width: 600px; margin: auto; background-color: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); padding: 40px;'>";

		html += "<h2 style='color: #2c3e50; text-align: center; margin-bottom: 30px;'>🎁 기프티콘이 도착했습니다!</h2>";
		html += "<p style='font-size: 16px; color: #444; line-height: 1.6;'>안녕하세요, <strong>mogatshoo</strong>입니다.<br>회원님께서 구매하신 기프티콘을 아래와 같이 보내드립니다.</p>";

		html += "<div style='text-align: center; margin: 30px 0;'>";
		html += "<img src='" + imgUrl
				+ "' alt='기프티콘 이미지' style='max-width: 100%; height: auto; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'/>";
		html += "</div>";

		html += "<p style='font-size: 14px; color: #888;'>※ 본 이메일은 회원님의 요청에 따라 발송되었으며, 개인정보 보호를 위해 외부에 공개되지 않습니다.</p>";
		html += "<p style='font-size: 14px; color: #999;'>※ 본 메일은 발신 전용입니다. 문의 사항은 <a href='https://mogatshoo.onrender.com/qanda/user' style='color: #3498db; text-decoration: none;'>고객센터</a>를 이용해주세요.</p>";

		html += "<p style='font-size: 14px; color: #555; margin-top: 30px;'>감사합니다.<br><strong>mogatshoo 드림</strong></p>";
		html += "</div>";
		html += "</body></html>";

		String email = pointItemSendLogEntity.getMemberEmail();

		mailService.sendGiftImg(title, email, html);
	}

}
