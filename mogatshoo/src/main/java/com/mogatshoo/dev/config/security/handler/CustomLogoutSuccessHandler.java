package com.mogatshoo.dev.config.security.handler; // SecurityConfig와 동일한 handler 패키지에 두는 것이 좋습니다.

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component; // 빈으로 등록하기 위해 추가

import java.io.IOException;

@Component // Spring 빈으로 등록
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String INTRO_SEEN_SESSION_KEY = "introSeen"; // 인트로 플래그 세션 키

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(false); // 기존 세션이 있으면 가져옴

        if (session != null) {
            // 인트로 플래그 제거
            session.removeAttribute(INTRO_SEEN_SESSION_KEY);
            System.out.println("CustomLogoutSuccessHandler: 세션에서 인트로 플래그 제거됨.");
        }

        // 로그아웃 후 원하는 페이지로 리다이렉트
        // 여기서는 로그인 페이지로 리다이렉트하도록 설정합니다.
        response.sendRedirect("/member/login");
    }
}