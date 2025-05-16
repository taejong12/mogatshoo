package com.mogatshoo.dev.hair_loss_test.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;


@Controller
@RequestMapping("hairLossTest")
public class HairLossTestController {
	
	@Autowired
	HairLossTestService service;
	@Autowired
	MemberService memberService;
	
	@GetMapping("/testHair")
	public String selectHair(@ModelAttribute("MemberEntity") MemberEntity member, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String memberId = authentication.getName();
		
		System.out.println("memberId : "+ memberId);
		member = memberService.findByMemberId(memberId);
		model.addAttribute("member", member);
		return "hair/hairLossTest";
	}
	
	    
	 /**
	     * AJAX 요청을 처리하는 API 엔드포인트
	     * 
	     * @param id 회원 ID (닉네임)
	     * @param file 업로드된 이미지 파일
	     * @param predictionData 예측 결과 JSON 데이터
	     * @return 저장 결과 및 필요한 데이터
	     */
	    @PostMapping("/api")
	    public ResponseEntity<?> saveHairLossTestResult(
	            @RequestParam("id") String id,
	            @RequestParam("files") MultipartFile file,
	            @RequestParam("predictionData") String predictionData) {
	        
	        Map<String, Object> response = new HashMap<>();
	        
	        try {
	           
	            
	            // 파일 유효성 검사
	            if (file == null || file.isEmpty()) {
	                throw new IllegalArgumentException("이미지 파일은 필수입니다");
	            }
	            
	            // predictionData 유효성 검사
	            if (predictionData == null || predictionData.trim().isEmpty()) {
	                throw new IllegalArgumentException("예측 데이터가 없습니다");
	            }
	            
	            // 예측 데이터에서 최고 확률의 클래스 이름 추출
	            String hairStage = extractHairStageFromPrediction(predictionData);
	            
	            // 서비스 호출하여 결과 저장
	            String savedFileName = service.saveHairLossTestResult(id, file, hairStage);
	            
	            // 성공 응답 생성
	            response.put("success", true);
	            response.put("memberId", id);
	            response.put("hairStage", hairStage);
	            response.put("imagePath", savedFileName);
	            response.put("message", "탈모 테스트 결과가 성공적으로 저장되었습니다.");
	            
	            return ResponseEntity.ok(response);
	            
	        } catch (IOException e) {
	            response.put("success", false);
	            response.put("error", "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
	            return ResponseEntity.badRequest().body(response);
	            
	        } catch (Exception e) {
	            response.put("success", false);
	            response.put("error", "처리 중 오류가 발생했습니다: " + e.getMessage());
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
	    
	    /**
	     * 예측 데이터 JSON에서 탈모 단계를 추출합니다.
	     * 
	     * @param predictionData 예측 데이터 JSON 문자열
	     * @return 탈모 단계
	     */
	    private String extractHairStageFromPrediction(String predictionData) {
	        try {
	            ObjectMapper mapper = new ObjectMapper();
	            JsonNode rootNode = mapper.readTree(predictionData);
	            
	            // 가장 높은 확률의 클래스 이름 추출
	            if (rootNode.isArray() && rootNode.size() > 0) {
	                return rootNode.get(0).get("className").asText();
	            }
	            
	            return ".";  // 기본값
	            
	        } catch (Exception e) {
	            // 파싱 실패 시 기본값 반환
	            System.err.println("예측 데이터 파싱 오류: " + e.getMessage());
	            return ".";
	        }
	    }
}
