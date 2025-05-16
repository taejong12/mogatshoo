package com.mogatshoo.dev.config.security.handler;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mogatshoo.dev.member.entity.MemberEntity;


public class MemberUserDetails implements UserDetails{

	private final MemberEntity memberEntity;

    public MemberUserDetails(MemberEntity memberEntity) {
        this.memberEntity = memberEntity;
    }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + memberEntity.getRole()) );
	}

	@Override
	public String getPassword() {
		return memberEntity.getMemberPwd();
	}

	@Override
	public String getUsername() {
		return memberEntity.getMemberId();
	}
	
	public String getNickName() {
		return memberEntity.getMemberNickName();
	}
}
