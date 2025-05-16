package com.mogatshoo.dev.config.security.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mogatshoo.dev.point.service.PointService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler{

	@Autowired
	private PointService pointService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// 로그인된 사용자 ID 가져오기
        String memberId = authentication.getName(); 

        // 출석 포인트 처리
        pointService.checkAttendancePoint(memberId);
        
        // 로그인 후 이동할 경로로 리다이렉트
        response.sendRedirect("/");
	}
}
