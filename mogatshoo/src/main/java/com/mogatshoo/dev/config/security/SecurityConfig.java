package com.mogatshoo.dev.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mogatshoo.dev.config.security.handler.LoginSuccessHandler;
import com.mogatshoo.dev.config.security.handler.MemberAccessDeniedHandler;
import com.mogatshoo.dev.config.security.handler.OAuth2LoginSuccessHandler;
import com.mogatshoo.dev.member.service.MemberServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    @Autowired
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
    
    @Autowired
    private MemberAccessDeniedHandler memberAccessDeniedHandler;
    
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

		http
			// CSRF 비활성화
			.csrf(csrf -> csrf.disable())
			
			// 모두 접근 가능한 경로
			.authorizeHttpRequests(request -> request
				.requestMatchers(new AntPathRequestMatcher("/")
							,new AntPathRequestMatcher("/member/idCheck")
							,new AntPathRequestMatcher("/member/emailCheck")
							,new AntPathRequestMatcher("/member/nickNameCheck")
							,new AntPathRequestMatcher("/member/sendEmail")
							,new AntPathRequestMatcher("/member/emailAuthCodeConfirm")
							,new AntPathRequestMatcher("/member/findByIdEmailCheck")
							,new AntPathRequestMatcher("/member/idAndEmailCheck")
							,new AntPathRequestMatcher("/member/pwdUpdate")
							,new AntPathRequestMatcher("/img/**")
							,new AntPathRequestMatcher("/videos/**")
							,new AntPathRequestMatcher("/css/**")
							,new AntPathRequestMatcher("/js/**")
							,new AntPathRequestMatcher("/favicon.ico")
							,new AntPathRequestMatcher("/fragments/**")
							).permitAll()
				
				// 로그인하지 않은 사용자만 접근 가능한 경로
				.requestMatchers(
						new AntPathRequestMatcher("/member/join")
						,new AntPathRequestMatcher("/member/complete")
						,new AntPathRequestMatcher("/member/lost")
						,new AntPathRequestMatcher("/member/findById")
						,new AntPathRequestMatcher("/member/findByPwd")
						,new AntPathRequestMatcher("/member/pwdUpdateForm")
						,new AntPathRequestMatcher("/oauth2/join")
						).anonymous()
				
				// 관리자만 접근 가능
				.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
				
				// 그 외 모든 요청은 인증 필요(로그인)
				.anyRequest().authenticated())
			
			// 에러 처리(주로 403 에러)
			.exceptionHandling(exception -> exception
					.accessDeniedHandler(memberAccessDeniedHandler))
			
			// 로그인 처리
			.formLogin(form -> form
					.loginPage("/member/login")
					.loginProcessingUrl("/member/login")
					.usernameParameter("memberId")
					.passwordParameter("memberPwd")
					.successHandler(loginSuccessHandler)
					.failureUrl("/member/login?error")
					.permitAll())
			
			// 외부 로그인 처리
			.oauth2Login(oauth -> oauth
					.loginPage("/member/login")
					.userInfoEndpoint(userInfo -> userInfo
							.userService(memberServiceImpl))
					.successHandler(oauth2LoginSuccessHandler)
					.permitAll())
			
			// 로그아웃 처리
			.logout(logout -> logout
					.logoutSuccessUrl("/member/login")
					.deleteCookies("JSESSIONID")
					.permitAll());

		return http.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
}
