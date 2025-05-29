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
		
		member = memberService.findByMemberId(memberId);
		model.addAttribute("member", member);
		return "hair/hairLossTest";
	}
	
	    
	 
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
	        
	        // 젤 높은 단계의 이름 추출하기
	        String hairStage = extractHairStageFromPrediction(predictionData);
	        
	        // 서비스 호출하여 결과 저장
	        String savedFileName = service.saveHairLossTestResult(id, file, hairStage);
	        
	        // 나머지 멘트는 스크립트에서 처리했어요
	        response.put("success", true);
	        response.put("memberId", id);
	        response.put("hairStage", hairStage);
	        response.put("imagePath", savedFileName);
	        
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
	     */
	    private String extractHairStageFromPrediction(String predictionData) {
	        try {
	            ObjectMapper mapper = new ObjectMapper();
	            JsonNode rootNode = mapper.readTree(predictionData);
	            
	            // 가장 높은 확률의 클래스 이름 추출
	            if (rootNode.isArray() && rootNode.size() > 0) {
	                return rootNode.get(0).get("className").asText();
	            }
	            
	            return ".";  
	            
	        } catch (Exception e) {
	            System.err.println("이거 안되면 집감: " + e.getMessage());
	            return ".";
	        }
	    }
}
