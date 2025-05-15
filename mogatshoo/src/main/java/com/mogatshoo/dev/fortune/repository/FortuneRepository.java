package com.mogatshoo.dev.fortune.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.fortune.entity.FortuneEntity;

public interface FortuneRepository extends JpaRepository<FortuneEntity, Integer> {
	
}
