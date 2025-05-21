package com.mogatshoo.dev.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		
		// 스프링 시큐리티에서 로그인은 formLogin로 처리하기 때문에
		// 1. 컨트롤러
		// 2. 인터셉터
		// 두 가지 방식 중 하나로 접근 허용을 막아줘야 함
		// 현재 로그인처리를 컨트롤러에서 하지 않으므로 인터셉터 사용함
        registry.addInterceptor(new LoginPageBlockInterceptor())
                .addPathPatterns("/member/login");
    }
}
