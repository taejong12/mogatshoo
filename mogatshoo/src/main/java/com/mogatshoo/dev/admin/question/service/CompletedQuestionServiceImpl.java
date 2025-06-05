package com.mogatshoo.dev.admin.question.service;

import com.mogatshoo.dev.admin.question.entity.CompletedQuestionEntity;
import com.mogatshoo.dev.admin.question.repository.CompletedQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompletedQuestionServiceImpl implements CompletedQuestionService {
    
    @Autowired
    private CompletedQuestionRepository completedQuestionRepository;
    
    @Override
    public List<CompletedQuestionEntity> getAllCompletedQuestions() {
        return completedQuestionRepository.findAll();
    }
    
    @Override
    public CompletedQuestionEntity getCompletedQuestionBySerialNumber(String serialNumber) {
        return completedQuestionRepository.findById(serialNumber).orElse(null);
    }
    
    @Override
    public Page<CompletedQuestionEntity> getCompletedQuestionsWithPaging(Pageable pageable) {
        return completedQuestionRepository.findAll(pageable);
    }
    
    @Override
    public Page<CompletedQuestionEntity> searchCompletedQuestions(String keyword, String publicStatus, 
                                                                LocalDate createdDate, LocalDate completedDate, 
                                                                Pageable pageable) {
        // 검색 로직 구현 (필요시)
        return completedQuestionRepository.findAll(pageable);
    }
}