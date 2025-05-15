package com.mogatshoo.dev.common.mail.service;

public interface EmailService {

	void sendAuthEmail(String title, String memberMail, String html);

	void findByIdSendEmail(String title, String memberEmail, String html);
}
