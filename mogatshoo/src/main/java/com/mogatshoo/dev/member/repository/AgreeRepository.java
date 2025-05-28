package com.mogatshoo.dev.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.member.entity.AgreeEntity;

public interface AgreeRepository extends JpaRepository<AgreeEntity, String> {
	
}
