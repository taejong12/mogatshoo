package com.mogatshoo.dev.member.service;

import java.util.List;

import com.mogatshoo.dev.member.entity.AgreeEntity;
import com.mogatshoo.dev.member.entity.MemberEntity;

public interface MemberService {

	void memberSave(MemberEntity memberEntity, AgreeEntity agreeEntity);

	Boolean memberIdCheck(String memberId);

	Boolean memberEmailCheck(String memberEmail);

	MemberEntity findByProviderAndProviderId(String provider, String providerId);

	MemberEntity findByMemberId(String memberId);

	void memberDelete(String memberId);

	void memberUpdate(MemberEntity memberEntity);

	MemberEntity findByIdEmailCheck(String memberEmail);

	MemberEntity findByMemberEmail(String memberEmail);

	MemberEntity findByPwdIdAndEmailCheck(String memberId, String memberEmail);

	void pwdUpdate(MemberEntity memberEntity);

	Boolean memberNickNameCheck(String memberNickName);
	
	List<MemberEntity> getAllMembers();
}
