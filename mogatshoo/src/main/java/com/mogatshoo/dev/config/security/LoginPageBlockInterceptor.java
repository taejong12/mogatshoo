package com.mogatshoo.dev.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginPageBlockInterceptor implements HandlerInterceptor {
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 상태이고 로그인/회원가입 페이지 요청일 경우 리다이렉트
        // Spring Security에서 비로그인 사용자의 principal은 "anonymousUser"
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}
