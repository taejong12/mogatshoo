package com.mogatshoo.dev.common.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CommonUserName {

	public String getUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			// 아이디 또는 고유값
			return userDetails.getUsername();
		}

		// 로그인 안 된 경우
		return null;
	}
}
