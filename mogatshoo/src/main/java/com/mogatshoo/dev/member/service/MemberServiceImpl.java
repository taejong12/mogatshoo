package com.mogatshoo.dev.member.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.config.security.handler.MemberUserDetails;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.repository.MemberRepository;
import com.mogatshoo.dev.point.service.PointService;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class MemberServiceImpl implements MemberService, UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private PointService pointService;
	
	@Autowired
	private HairLossTestService hairLossTestService;
	
	@Override
	public void memberSave(MemberEntity memberEntity) {
		if(memberEntity.getProviderId() == null) {			
			memberEntity.setMemberPwd(passwordEncoder.encode(memberEntity.getMemberPwd()));
		} else {
			memberEntity.setMemberPwd(passwordEncoder.encode(UUID.randomUUID().toString()));
		}
		memberRepository.save(memberEntity);
		
		// 포인트 컬럼 생성
		String memberId = memberEntity.getMemberId();
		pointService.memberJoinPointSave(memberId);
	}

	@Override
	public Boolean memberIdCheck(String memberId) {
		return memberRepository.findById(memberId).isEmpty();
	}

	@Override
	public Boolean memberEmailCheck(String memberEmail) {
		return memberRepository.findByMemberEmail(memberEmail).isEmpty();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
        MemberEntity memberEntity = memberRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return new MemberUserDetails(memberEntity);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		
		// 사용자 정보 가져오기
		OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
		
		String provider = userRequest.getClientRegistration().getRegistrationId();
		
		Map<String, Object> attributes = oauth2User.getAttributes();
        
		if("kakao".equals(provider)) {
			String providerId = attributes.get("id").toString();
			
			Map<String, Object> map = new HashMap<>();
			map.put("provider", provider);
			map.put("providerId", providerId);
			
			return new DefaultOAuth2User(
						// 권한 목록
						Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
						// 속성
						map,
						// 사용자 이름 key -> user.getName() 에 사용될 키
						"providerId"
					);
		} else if("naver".equals(provider)) {
			
			// 네이버는 "response" 키 안에 사용자 정보가 있음
		    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		    String providerId = response.get("id").toString();
		    String email = response.get("email").toString();
		    String name = response.get("name").toString();
		    String birthyear = response.get("birthyear").toString();
		    String birthday = response.get("birthday").toString();
		    String mobile = response.get("mobile").toString();
		    String gender = response.get("gender").toString();
		    String birth = birthyear + "-" + birthday;
		    
		    Map<String, Object> map = new HashMap<>();
			map.put("provider", provider);
			map.put("providerId", providerId);
			map.put("email", email);
			map.put("name", name);
			map.put("birth", birth);
			map.put("mobile", mobile);
			map.put("gender", gender);
		    
		    return new DefaultOAuth2User(
					// 권한 목록
					Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
					// 속성
					map,
					// 사용자 이름 key -> user.getName() 에 사용될 키
					"providerId"
				);
		    
		} else {
			Map<String, Object> map = new HashMap<>();
			String providerId = attributes.get("sub").toString();
			String name = attributes.get("name").toString();
			String email = attributes.get("email").toString();
			
			map.put("provider", provider);
			map.put("providerId", providerId);
			map.put("name", name);
			map.put("email", email);
			
			return new DefaultOAuth2User(
					// 권한 목록
					Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
					// 속성
					map,
					// 사용자 이름 key -> user.getName() 에 사용될 키
					"providerId"
				);
		}
	}

	@Override
	public MemberEntity findByProviderAndProviderId(String provider, String providerId) {
		return memberRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
	}

	@Override
	public MemberEntity findByMemberId(String memberId) {
		return memberRepository.findById(memberId).orElse(null);
	}

	@Override
	public void memberDelete(String memberId) {
		pointService.memberDelete(memberId);
		hairLossTestService.memberDelete(memberId);
		
		memberRepository.deleteById(memberId);
	}

	@Override
	public void memberUpdate(MemberEntity memberEntity) {
		
		MemberEntity member = memberRepository.findById(memberEntity.getMemberId())
		        .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

		member.setMemberNickName(memberEntity.getMemberNickName());
		member.setMemberName(memberEntity.getMemberName());
		member.setMemberZipcode(memberEntity.getMemberZipcode());
		member.setMemberAddress1(memberEntity.getMemberAddress1());
		member.setMemberAddress2(memberEntity.getMemberAddress2());
		member.setMemberTel(memberEntity.getMemberTel());
	}

	@Override
	public MemberEntity findByIdEmailCheck(String memberEmail) {
		return memberRepository.findByMemberEmail(memberEmail).orElse(null);
	}

	@Override
	public MemberEntity findByMemberEmail(String memberEmail) {
		return memberRepository.findByMemberEmail(memberEmail).orElse(null);
	}

	@Override
	public MemberEntity findByPwdIdAndEmailCheck(String memberId, String memberEmail) {
		return memberRepository.findByMemberIdAndMemberEmail(memberId, memberEmail).orElse(null);
	}

	@Override
	public void pwdUpdate(MemberEntity memberEntity) {
		String pwdInput = passwordEncoder.encode(memberEntity.getMemberPwd());
		
		MemberEntity member = memberRepository.findById(memberEntity.getMemberId())
		        .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

		member.setMemberPwd(pwdInput);
	}

	@Override
	public Boolean memberNickNameCheck(String memberNickName) {
		return memberRepository.findByMemberNickName(memberNickName).isEmpty();
	}
}
