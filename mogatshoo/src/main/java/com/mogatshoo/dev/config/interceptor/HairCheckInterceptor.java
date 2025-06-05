package com.mogatshoo.dev.config.interceptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HairCheckInterceptor implements HandlerInterceptor {

	@Autowired
	private HairLossTestService hairLossTestService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws IOException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// 로그인 안된 상태거나 익명 사용자라면 통과
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
			return true;
		}

		String memberId = auth.getName();

		// 권한에 따라 리다이렉트
		for (GrantedAuthority authority : auth.getAuthorities()) {
			String role = authority.getAuthority();

			if (role.equals("ROLE_USER")) {
				// 탈모 진단 여부 확인
				boolean hairCheck = hairLossTestService.loginMemberHairCheck(memberId);

				if (!hairCheck && !request.getRequestURI().startsWith("/hairLossTest")) {
					response.sendRedirect("/hairLossTest/testHair");
					return false;
				}
			}
		}

		return true;
	}
}
