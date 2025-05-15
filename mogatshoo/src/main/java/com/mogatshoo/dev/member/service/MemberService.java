package com.mogatshoo.dev.member.service;

import com.mogatshoo.dev.member.entity.MemberEntity;

public interface MemberService {

	void memberSave(MemberEntity memberEntity);

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

}
