package com.mogatshoo.dev.admin.question.repository;

import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedQuestionRepository extends JpaRepository<CompletedQuestionEntity, String> {
	
}