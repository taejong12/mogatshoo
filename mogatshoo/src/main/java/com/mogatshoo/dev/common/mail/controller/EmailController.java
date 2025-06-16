package com.mogatshoo.dev.common.mail.controller;

import java.util.Map;

import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;
import com.mogatshoo.dev.member.entity.MemberEntity;

import jakarta.servlet.http.HttpSession;

public interface EmailController {

	Map<String, Object> sendAuthEmail(String memberEmail, HttpSession session);
	
	Map<String, Object> authCodeConfirm(String memberAuthCode, HttpSession session);

	void findByIdSendEmail(MemberEntity memberEntity);

	void sendGiftImg(PointItemSendLogEntity pointItemSendLogEntity);
	
}