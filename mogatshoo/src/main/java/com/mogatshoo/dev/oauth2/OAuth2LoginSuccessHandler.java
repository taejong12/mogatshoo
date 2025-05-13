package com.mogatshoo.dev.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler{

	@Autowired
	private MemberService memberService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// 인증 완료된 사용자 정보 가져오기
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String provider = oauth2User.getAttribute("provider");
        String providerId = oauth2User.getAttribute("providerId");
        
        MemberEntity memberEntity = memberService.findByProviderAndProviderId(provider, providerId);
        
        if (memberEntity != null) {
        	
            // 기존 회원: 로그인 처리
            UserDetails userDetails = new OAuth2UserDetails(memberEntity);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(auth);

            response.sendRedirect("/");
            
        } else {
        	// 자동 로그인 막기: 컨텍스트 초기화
        	SecurityContextHolder.clearContext();
        	request.getSession().invalidate();
        	
            // 신규 회원: 회원가입 폼 이동
            HttpSession session = request.getSession();
            session.setAttribute("oauth2User", oauth2User);
            
            response.sendRedirect("/oauth2/join");
        } 
	}
}
