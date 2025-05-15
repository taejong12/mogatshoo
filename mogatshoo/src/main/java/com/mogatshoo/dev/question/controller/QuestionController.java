package com.mogatshoo.dev.question.controller;

import com.mogatshoo.dev.question.entity.QuestionEntity;
import com.mogatshoo.dev.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/questions")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    // 질문 목록 페이지
    @GetMapping
    public String getAllQuestions(Model model) {
        model.addAttribute("questions", questionService.getAllQuestions());
        return "question/list";
    }
    
    // 새 질문 생성 페이지
    @GetMapping("/new")
    public String newQuestionForm(Model model) {
        // 빈 질문 객체 생성
        QuestionEntity question = new QuestionEntity();
        
        // 다음 일련번호 생성
        String nextSerialNumber = questionService.generateNextSerialNumber();
        question.setSerialNumber(nextSerialNumber);
        
        model.addAttribute("question", question);
        return "question/create";
    }
    
    // 질문 저장
    @PostMapping
    public String createQuestion(@ModelAttribute QuestionEntity question) {
        questionService.createQuestion(question);
        return "redirect:/questions";
    }
    
    // 질문 상세 페이지
    @GetMapping("/{serialNumber}")
    public String getQuestion(@PathVariable("serialNumber") String serialNumber, Model model) {
        model.addAttribute("question", questionService.getQuestionBySerialNumber(serialNumber));
        return "question/detail";
    }
    
    // 질문 수정 처리 - 추가된 메서드
    @PostMapping("/{serialNumber}")
    public String updateQuestion(@PathVariable("serialNumber") String serialNumber, 
                               @ModelAttribute QuestionEntity questionForm) {
        try {
            // 기존 질문 가져오기
            QuestionEntity existingQuestion = questionService.getQuestionBySerialNumber(serialNumber);
            
            // 폼에서 입력받은 값을 기존 질문에 복사
            existingQuestion.setQuestion(questionForm.getQuestion());
            existingQuestion.setOption1(questionForm.getOption1());
            existingQuestion.setOption2(questionForm.getOption2());
            existingQuestion.setOption3(questionForm.getOption3());
            existingQuestion.setOption4(questionForm.getOption4());
            
            // 공개 상태도 폼에서 받은 값으로 업데이트
            existingQuestion.setIsPublic(questionForm.getIsPublic());
            
            // 수정된 기존 질문 저장
            questionService.updateQuestion(existingQuestion);
            
            return "redirect:/questions";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    
    // 관리자용 공개 여부 변경
    @PostMapping("/{serialNumber}/public")
    public String updatePublicStatus(@PathVariable("serialNumber") String serialNumber, 
                                   @RequestParam("isPublic") String isPublic) {
        questionService.updatePublicStatus(serialNumber, isPublic);
        return "redirect:/questions/" + serialNumber;
    }
}