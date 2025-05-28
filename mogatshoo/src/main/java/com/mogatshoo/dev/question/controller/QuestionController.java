package com.mogatshoo.dev.question.controller;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.question.entity.QuestionEntity;
import com.mogatshoo.dev.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private HairLossTestService pictureService;
    
    @Autowired
    private MemberService memberService;
    
    // 질문 목록 페이지 - 경로 수정
    @GetMapping({"", "/", "/list"})
    public String getAllQuestions(Model model) {
        List<QuestionEntity> questions = questionService.getAllQuestionsWithFixedImagePaths();
        model.addAttribute("questions", questions);
        return "question/list";
    }

    @GetMapping("/new")
    public String newQuestionForm(Model model, @ModelAttribute("MemberEntity") MemberEntity member) {
        try {
            // 빈 질문 객체 생성
            QuestionEntity question = new QuestionEntity();
            System.out.println("Question 객체 생성됨: " + question);

            // 다음 일련번호 생성
            String nextSerialNumber = questionService.generateNextSerialNumber();
            question.setSerialNumber(nextSerialNumber);
            System.out.println("일련번호 설정됨: " + nextSerialNumber);
            
            // 기본값은 항상 비공개로 설정
            question.setIsPublic("no");

            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String memberId = authentication.getName();
            
            System.out.println("memberId : "+ memberId);
            member = memberService.findByMemberId(memberId);
            model.addAttribute("member", member);
            
            // 랜덤으로 4개의 사진 가져오기
            List<PictureEntity> fetchedPictures = pictureService.getRandomPictures(4);
            List<PictureEntity> randomPictures = new ArrayList<>();
            
            // 사진과 연결된 회원 정보를 담을 맵
            Map<String, String> memberNicknames = new HashMap<>();
            
            if (fetchedPictures != null) {
                System.out.println("가져온 사진 수: " + fetchedPictures.size());

                for (int i = 0; i < fetchedPictures.size(); i++) {
                    PictureEntity pic = fetchedPictures.get(i);
                    if (pic != null) {
                        System.out.println("사진 " + i + " - ID: " + pic.getMemberId() + ", Path: " + pic.getHairPicture());

                        // 유효한 사진만 추가
                        if (pic.getHairPicture() != null && !pic.getHairPicture().isEmpty()) {
                            randomPictures.add(pic);
                            
                            // 회원 ID로 닉네임 조회 및 저장
                            try {
                                MemberEntity picMember = memberService.findByMemberId(pic.getMemberId());
                                if (picMember != null && picMember.getMemberName() != null) {
                                    memberNicknames.put(pic.getMemberId(), picMember.getMemberName());
                                } else {
                                    memberNicknames.put(pic.getMemberId(), "알 수 없음");
                                }
                            } catch (Exception e) {
                                System.err.println("회원 정보 조회 실패 [ID: " + pic.getMemberId() + "]: " + e.getMessage());
                                memberNicknames.put(pic.getMemberId(), "알 수 없음");
                            }
                        }
                    } else {
                        System.out.println("사진 " + i + "는 null입니다");
                    }
                }

                System.out.println("최종 유효한 사진 수: " + randomPictures.size());
            } else {
                System.out.println("가져온 사진 목록이 null입니다");
            }

            model.addAttribute("question", question);
            model.addAttribute("randomPictures", randomPictures);
            model.addAttribute("memberNicknames", memberNicknames);

            System.out.println("모델에 추가된 사진 목록 크기: " + randomPictures.size());
            System.out.println("모델에 추가된 닉네임 정보 크기: " + memberNicknames.size());

            return "question/create";
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "질문 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    // 질문 저장
    @PostMapping
    public String createQuestion(@ModelAttribute QuestionEntity question,
                                @RequestParam(required = false) String imageReference1,
                                @RequestParam(required = false) String imageReference2,
                                @RequestParam(required = false) String imageReference3,
                                @RequestParam(required = false) String imageReference4) {
        // isPublic 값이 없거나 유효하지 않은 경우, 기본값으로 'no' 설정
        if (question.getIsPublic() == null || (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
            question.setIsPublic("no");
        }

        // 이미지 경로가 있는 경우 옵션에 이미지 경로를 저장 (닉네임 대신)
        if (imageReference1 != null && !imageReference1.isEmpty()) {
            question.setOption1(imageReference1);
        }
        if (imageReference2 != null && !imageReference2.isEmpty()) {
            question.setOption2(imageReference2);
        }
        if (imageReference3 != null && !imageReference3.isEmpty()) {
            question.setOption3(imageReference3);
        }
        if (imageReference4 != null && !imageReference4.isEmpty()) {
            question.setOption4(imageReference4);
        }

        System.out.println("저장될 질문: " + question);
        questionService.createQuestion(question);
        
        try {
            String successMessage = URLEncoder.encode("새 질문이 성공적으로 생성되었습니다. (기본 상태: 비공개)", StandardCharsets.UTF_8.toString());
            return "redirect:/questions?status=success&message=" + successMessage;
        } catch (Exception e) {
            return "redirect:/questions";
        }
    }

    // 질문 수정 폼 페이지 - 기존 detail.html 사용
    @GetMapping("/{serialNumber}/edit")
    public String editQuestionForm(@PathVariable("serialNumber") String serialNumber, Model model) {
        try {
            System.out.println("수정 요청 받은 일련번호: " + serialNumber);
            
            QuestionEntity question = questionService.getQuestionBySerialNumber(serialNumber);
            System.out.println("조회된 질문: " + question);

            // 상태가 null이거나 유효하지 않은 경우 기본값으로 설정
            if (question.getIsPublic() == null || (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
                question.setIsPublic("no");
            }

            model.addAttribute("question", question);
            return "question/detail"; // 기존 detail.html 사용
        } catch (Exception e) {
            System.err.println("질문 조회 실패: " + e.getMessage());
            e.printStackTrace();
            try {
                String errorMessage = URLEncoder.encode("질문을 찾을 수 없습니다.", StandardCharsets.UTF_8.toString());
                return "redirect:/questions?status=error&message=" + errorMessage;
            } catch (Exception ex) {
                return "redirect:/questions";
            }
        }
    }

    // 질문 상세 페이지 (필요한 경우)
    @GetMapping("/{serialNumber}")
    public String getQuestion(@PathVariable("serialNumber") String serialNumber, Model model) {
        QuestionEntity question = questionService.getQuestionBySerialNumber(serialNumber);

        // 상태가 null이거나 유효하지 않은 경우 기본값으로 설정
        if (question.getIsPublic() == null || (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
            question.setIsPublic("no");
        }

        model.addAttribute("question", question);
        return "question/detail";
    }

    // 공개 상태만 변경하는 API 엔드포인트
    @PostMapping("/{serialNumber}/visibility")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVisibility(
            @PathVariable("serialNumber") String serialNumber,
            @RequestParam("isPublic") String isPublic) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 유효성 검사
            if (!isPublic.equals("yes") && !isPublic.equals("no")) {
                response.put("success", false);
                response.put("message", "잘못된 공개 상태 값입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 질문 조회
            QuestionEntity question = questionService.getQuestionBySerialNumber(serialNumber);
            if (question == null) {
                response.put("success", false);
                response.put("message", "질문을 찾을 수 없습니다: " + serialNumber);
                return ResponseEntity.notFound().build();
            }
            
            // 현재 상태와 동일하면 불필요한 업데이트 방지
            if (question.getIsPublic().equals(isPublic)) {
                response.put("success", true);
                response.put("message", "이미 " + (isPublic.equals("yes") ? "공개" : "비공개") + " 상태입니다.");
                response.put("currentStatus", isPublic);
                return ResponseEntity.ok(response);
            }
            
            // 상태 업데이트
            question.setIsPublic(isPublic);
            questionService.updateQuestion(question);
            
            // 성공 응답
            response.put("success", true);
            response.put("message", "질문이 " + (isPublic.equals("yes") ? "공개" : "비공개") + " 상태로 변경되었습니다.");
            response.put("currentStatus", isPublic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상태 변경 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 질문 수정 처리
    @PostMapping("/{serialNumber}")
    public String updateQuestion(@PathVariable("serialNumber") String serialNumber, 
            @ModelAttribute QuestionEntity questionForm) {
        try {
            // 기존 질문 가져오기
            QuestionEntity existingQuestion = questionService.getQuestionBySerialNumber(serialNumber);
            
            // 공개 상태 변경 여부 확인
            boolean isPublicChanged = !existingQuestion.getIsPublic().equals(questionForm.getIsPublic());

            // 폼에서 입력받은 값을 기존 질문에 복사
            existingQuestion.setQuestion(questionForm.getQuestion());
            existingQuestion.setOption1(questionForm.getOption1());
            existingQuestion.setOption2(questionForm.getOption2());
            existingQuestion.setOption3(questionForm.getOption3());
            existingQuestion.setOption4(questionForm.getOption4());

            // 공개 상태 업데이트
            String isPublic = questionForm.getIsPublic();
            if (isPublic == null || (!isPublic.equals("yes") && !isPublic.equals("no"))) {
                isPublic = "no";
            }
            existingQuestion.setIsPublic(isPublic);

            // 수정된 기존 질문 저장
            questionService.updateQuestion(existingQuestion);

            // 성공 메시지 구성
            String successMessage;
            if (isPublicChanged) {
                if (isPublic.equals("yes")) {
                    successMessage = String.format("질문 %s이(가) 수정되었으며, 비공개에서 공개 상태로 변경되었습니다.", serialNumber);
                } else {
                    successMessage = String.format("질문 %s이(가) 수정되었으며, 공개에서 비공개 상태로 변경되었습니다.", serialNumber);
                }
            } else {
                successMessage = String.format("질문 %s이(가) 성공적으로 수정되었습니다.", serialNumber);
            }
            
            return "redirect:/questions?status=success&message=" + URLEncoder.encode(successMessage, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                String errorMessage = URLEncoder.encode("질문 수정 중 오류가 발생했습니다.", StandardCharsets.UTF_8.toString());
                return "redirect:/questions?status=error&message=" + errorMessage;
            } catch (Exception ex) {
                return "redirect:/error";
            }
        }
    }

    // 질문 삭제 처리
    @GetMapping("/{serialNumber}/delete")
    public String deleteQuestion(@PathVariable("serialNumber") String serialNumber) {
        try {
            System.out.println("삭제 요청 받음: " + serialNumber);
            questionService.deleteQuestion(serialNumber);
            System.out.println("삭제 완료: " + serialNumber);

            String successMessage = URLEncoder.encode("질문이 성공적으로 삭제되었습니다.", StandardCharsets.UTF_8.toString());
            return "redirect:/questions?status=success&message=" + successMessage;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("삭제 실패: " + e.getMessage());

            try {
                String errorMessage = URLEncoder.encode("질문 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8.toString());
                return "redirect:/questions?status=error&message=" + errorMessage;
            } catch (Exception ex) {
                return "redirect:/error";
            }
        }
    }
}