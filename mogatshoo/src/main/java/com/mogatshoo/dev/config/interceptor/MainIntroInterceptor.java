package com.mogatshoo.dev.config.interceptor; // 당신의 인터셉터 패키지

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MainIntroInterceptor implements HandlerInterceptor {

	 private static final String INTRO_SEEN_SESSION_KEY = "introSeen";

	    @Override
	    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	        String requestUri = request.getRequestURI();
	        HttpSession session = request.getSession();

	        // 1. 현재 인증된 사용자인지 확인 (로그인 여부)
	        // Spring Security context에서 Authentication 객체를 가져옵니다.
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated() &&
	                                   !"anonymousUser".equals(authentication.getPrincipal())); // 익명 사용자가 아닌 경우

	        // 2. 요청 URI가 루트("/")이고
	        // 3. 사용자가 로그인된 상태이며
	        // 4. 세션에 "introSeen" 플래그가 없는 경우 (인트로를 아직 안 봤음)
	        if ("/".equals(requestUri) && isAuthenticated && session.getAttribute(INTRO_SEEN_SESSION_KEY) == null) {
	            System.out.println("IntroInterceptor: Logged-in user has not seen intro in this session. Redirecting to /intro.");
	            response.sendRedirect("/intro"); // /intro 페이지로 리다이렉트
	            return false; // 더 이상 요청 처리를 진행하지 않음
	        }

	        // 5. 인트로 페이지('/intro')로 이동하는 요청이라면 (직접 접근 또는 리다이렉트로)
	        //    세션에 "introSeen" 플래그를 설정합니다.
	        //    이것은 SecurityConfig에서 permitAll() 처리되었으므로 로그인 여부와 상관없이 호출될 수 있습니다.
	        //    따라서 여기서 플래그를 설정하는 것이 안전합니다.
	        if ("/intro".equals(requestUri)) {
	             session.setAttribute(INTRO_SEEN_SESSION_KEY, true);
	             System.out.println("IntroInterceptor: User is visiting intro page. Setting introSeen flag.");
	        }

	        // 요청 처리를 계속 진행
	        return true;
	    }
	}