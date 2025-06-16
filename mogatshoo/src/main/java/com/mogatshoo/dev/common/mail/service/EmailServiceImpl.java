package com.mogatshoo.dev.common.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Async
	@Override
	public void sendAuthEmail(String title, String memberMail, String html) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "utf-8");

			messageHelper.setFrom(new InternetAddress("mogatshoo@gmail.com", "mogatshoo"));
			messageHelper.setSubject(title);
			messageHelper.setTo(memberMail);
			messageHelper.setText(html, true);

			mailSender.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void findByIdSendEmail(String title, String memberEmail, String html) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "utf-8");

			messageHelper.setFrom(new InternetAddress("mogatshoo@gmail.com", "mogatshoo"));
			messageHelper.setSubject(title);
			messageHelper.setTo(memberEmail);
			messageHelper.setText(html, true);

			mailSender.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendGiftImg(String title, String email, String html) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "utf-8");

			messageHelper.setFrom(new InternetAddress("mogatshoo@gmail.com", "mogatshoo"));
			messageHelper.setSubject(title);
			messageHelper.setTo(email);
			messageHelper.setText(html, true);

			mailSender.send(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
