package com.mogatshoo.dev.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.member.entity.MemberEntity;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
	
	Optional<MemberEntity> findByMemberEmail(String memberEmail);

	Optional<MemberEntity> findByProviderAndProviderId(String provider, String providerId);

	Optional<MemberEntity> findByMemberEmailAndProvider(String memberEmail, String provider);

	Optional<MemberEntity> findByMemberIdAndMemberEmail(String memberId, String memberEmail);
}
