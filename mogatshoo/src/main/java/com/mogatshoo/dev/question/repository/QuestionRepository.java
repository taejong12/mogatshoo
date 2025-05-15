package com.mogatshoo.dev.question.repository;

import com.mogatshoo.dev.question.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, String> {
	
	// 가장 최근의 일련번호 조회
	@Query("SELECT MAX(q.serialNumber) FROM question q")
	String findMaxSerialNumber();
}