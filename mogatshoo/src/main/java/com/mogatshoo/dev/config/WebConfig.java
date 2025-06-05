package com.mogatshoo.dev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mogatshoo.dev.config.interceptor.HairCheckInterceptor;
import com.mogatshoo.dev.config.interceptor.LoginPageBlockInterceptor;


@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Autowired
    private HairCheckInterceptor hairCheckInterceptor;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		
		// 로그인한 회원 다시 로그인 페이지 접근 차단
		// 스프링 시큐리티에서 로그인은 formLogin로 처리하기 때문에
		// 1. 컨트롤러
		// 2. 인터셉터
		// 두 가지 방식 중 하나로 접근 허용을 막아줘야 함
		// 현재 로그인처리를 컨트롤러에서 하지 않으므로 인터셉터 사용함
        registry.addInterceptor(new LoginPageBlockInterceptor())
                .addPathPatterns("/member/login");
        
        // 탈모 진단 여부 확인 인터셉터
        registry.addInterceptor(hairCheckInterceptor)
	        .addPathPatterns("/**")
	        .excludePathPatterns(
	            "/css/**", "/js/**", "/img/**", "/videos/**", "/favicon.ico", "/my_model/**", "/hairLossTest/**" // 무한 루프 방지
	        );
    }
	
	 //Chrome DevTools .well-known 경고 제거
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // .well-known 경로 처리 (Chrome DevTools 경고 제거)
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/");
       
    }
}
