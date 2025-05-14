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

import com.mogatshoo.dev.member.service.MemberServiceImpl;
import com.mogatshoo.dev.oauth2.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MemberServiceImpl memberServiceImpl;

    @Autowired
    private OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

		// CSRF 비활성화
		http.csrf(csrf -> csrf.disable())
		
		// 요청 url 처리
		.authorizeHttpRequests(request -> request
				.requestMatchers(new AntPathRequestMatcher("/")
						,new AntPathRequestMatcher("/member/login")
						,new AntPathRequestMatcher("/member/join")
						,new AntPathRequestMatcher("/member/complete")
						,new AntPathRequestMatcher("/member/idCheck")
						,new AntPathRequestMatcher("/member/emailCheck")
						,new AntPathRequestMatcher("/member/sendEmail")
						,new AntPathRequestMatcher("/member/emailAuthCodeConfirm")
						,new AntPathRequestMatcher("/member/lost")
						,new AntPathRequestMatcher("/member/findById")
						,new AntPathRequestMatcher("/member/findByPwd")
						,new AntPathRequestMatcher("/member/idAndEmailCheck")
						,new AntPathRequestMatcher("/member/pwdUpdateForm")
						,new AntPathRequestMatcher("/member/pwdUpdate")
						,new AntPathRequestMatcher("/oauth2/join")
						,new AntPathRequestMatcher("/img/**")
						,new AntPathRequestMatcher("/css/**")
						,new AntPathRequestMatcher("/js/**")
						,new AntPathRequestMatcher("/fragments/**")
						).permitAll()
				.anyRequest().authenticated())
		
		// 로그인 처리
		.formLogin(form -> form
				.loginPage("/member/login")
				.loginProcessingUrl("/member/login")
				.usernameParameter("memberId")
				.passwordParameter("memberPwd")
				.defaultSuccessUrl("/", true)
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
