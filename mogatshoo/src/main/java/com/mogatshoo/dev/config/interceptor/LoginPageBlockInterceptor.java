package com.mogatshoo.dev.config.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        	// 권한에 따라 리다이렉트
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();

                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/main");
                    return false;
                } else if (role.equals("ROLE_USER")) {
                    response.sendRedirect("/");
                    return false;
                }
            }

            // 그 외 권한이 있는 경우
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}
