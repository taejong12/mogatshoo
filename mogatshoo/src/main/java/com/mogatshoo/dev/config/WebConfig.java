package com.mogatshoo.dev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mogatshoo.dev.config.interceptor.HairCheckInterceptor;
import com.mogatshoo.dev.config.interceptor.LoginPageBlockInterceptor;
import com.mogatshoo.dev.config.interceptor.MainIntroInterceptor;


@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Autowired
    private HairCheckInterceptor hairCheckInterceptor;
	@Autowired
    private MainIntroInterceptor introInterceptor; // IntroInterceptor 빈을 주입받음
	
	
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
        

   	 registry.addInterceptor(introInterceptor) // @Autowired로 주입받은 introInterceptor 사용
        .addPathPatterns("/**") // 모든 경로에 인터셉터 적용
        .excludePathPatterns(
            // 기존 HairCheckInterceptor의 excludePathPatterns를 참고하여
            // 중복되거나, Interceptor가 관여할 필요가 없는 경로들을 제외합니다.
            "/videos/**", "/css/**", "/js/**", "/img/**", "/favicon.ico", // 정적 리소스
            "/error", // 에러 페이지
            "/my_model/**", "/hairLossTest/**", // HairCheckInterceptor와 동일하게 제외 (무한 루프 방지)
            
            // SecurityConfig의 permitAll()에 있는 인증 관련 경로들 (LoginInterceptor와 무관하므로 제외)
            "/member/login", // 로그인 페이지
            "/member/join", "/member/complete", "/member/lost",
            "/member/findById", "/member/findByPwd", "/member/pwdUpdateForm", "/member/agree",
            "/oauth2/join", "/oauth2/authorization/**",
            "/member/idCheck", "/member/emailCheck", "/member/nickNameCheck",
            "/member/sendEmail", "/member/emailAuthCodeConfirm", "/member/findByIdEmailCheck",
            "/member/idAndEmailCheck", "/member/pwdUpdate",
            
            // 인증은 필요하지만 IntroInterceptor가 관여할 필요 없는 경로
            "/admin/**" // 관리자
        );

   // ========================================================================
   // 추가된 인터셉터는 이 코드 블록 아래에 있어야 합니다.
   // 인터셉터는 등록 순서대로 실행됩니다.
   // IntroInterceptor가 로그인 페이지 차단이나 탈모 진단 인터셉터보다 나중에 실행되도록 하는 것이 좋습니다.
   // 예를 들어, 로그인 안 한 사용자는 LoginPageBlockInterceptor -> HairCheckInterceptor -> IntroInterceptor (여기까지 오지 못함)
   // 로그인 한 사용자 (인트로 안 본 경우): HairCheckInterceptor -> IntroInterceptor
   // 이 순서로 동작하게 됩니다.
   // ========================================================================
   }
	
	//Chrome DevTools .well-known 경고 제거
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // .well-known 경로 처리 (Chrome DevTools 경고 제거)
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/");
       
    }
}
