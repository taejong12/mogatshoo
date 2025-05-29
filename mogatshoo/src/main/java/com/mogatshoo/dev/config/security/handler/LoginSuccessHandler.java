package com.mogatshoo.dev.config.security.handler;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.point.service.PointService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler{

	@Autowired
	private PointService pointService;
	
	@Autowired
	private HairLossTestService hairLossTestService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// 로그인된 사용자 ID 가져오기
        String memberId = authentication.getName(); 
        
        // 사용자 권한
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority(); 
            
            System.out.println("사용자 권한: " + role);
            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/main");
                return;
            } else if (role.equals("ROLE_USER")) {
            	// 출석 포인트 처리
                pointService.checkAttendancePoint(memberId);
                
                // 탈모진단 확인
                boolean hairCheck = hairLossTestService.loginMemberHairCheck(memberId);
                
                if(hairCheck) {
                	// 로그인 후 이동할 경로로 리다이렉트
                	response.sendRedirect("/");
                } else {
                	// 탈모진단페이지로 이동
                	response.sendRedirect("/hairLossTest/testHair");
                }
                return;
            }
        }
	}
}
