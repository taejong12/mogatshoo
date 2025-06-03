package com.mogatshoo.dev.admin.question.controller;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/questions")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private HairLossTestService pictureService;

	@Autowired
	private MemberService memberService;

	// 질문 목록 페이지 - 경로 수정
	@GetMapping({ "", "/", "/list" })
	public String getAllQuestions(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "publicStatus", required = false) String publicStatus,
			@RequestParam(value = "createdDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDate,
			@RequestParam(value = "votingDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate votingDate,
			Model model) {
		
		try {
			// 페이지 번호는 0부터 시작하므로 음수 방지
			if (page < 0) page = 0;
			
			// 페이지 크기 검증 (1~50 사이)
			if (size < 1) size = 10;
			if (size > 50) size = 50;
			
			// 페이징 객체 생성 - 최신 질문이 먼저 오도록 createdAt 내림차순 정렬
			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
			
			// 검색 조건이 있는지 확인
			boolean hasSearchCondition = (keyword != null && !keyword.trim().isEmpty()) ||
					(publicStatus != null && !publicStatus.trim().isEmpty()) ||
					(createdDate != null) ||
					(votingDate != null);
			
			Page<QuestionEntity> questionPage;
			
			if (hasSearchCondition) {
				// 검색 조건이 있는 경우 검색 실행
				questionPage = questionService.searchQuestions(keyword, publicStatus, createdDate, votingDate, pageable);
				System.out.println("=== 검색 실행 ===");
				System.out.println("키워드: " + keyword);
				System.out.println("공개상태: " + publicStatus);
				System.out.println("생성날짜: " + createdDate);
				System.out.println("투표날짜: " + votingDate);
			} else {
				// 검색 조건이 없는 경우 전체 목록 조회
				questionPage = questionService.getQuestionsWithPaging(pageable);
				System.out.println("=== 전체 목록 조회 ===");
			}
			
			// 페이징 정보를 모델에 추가
			model.addAttribute("questions", questionPage.getContent());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", questionPage.getTotalPages());
			model.addAttribute("totalElements", questionPage.getTotalElements());
			model.addAttribute("size", size);
			model.addAttribute("hasNext", questionPage.hasNext());
			model.addAttribute("hasPrevious", questionPage.hasPrevious());
			
			// 검색 조건을 모델에 추가 (검색 폼 유지용)
			model.addAttribute("keyword", keyword);
			model.addAttribute("publicStatus", publicStatus);
			model.addAttribute("createdDate", createdDate);
			model.addAttribute("votingDate", votingDate);
			
			// 페이지 번호 리스트 생성 (현재 페이지 주변 5개씩)
			List<Integer> pageNumbers = generatePageNumbers(page, questionPage.getTotalPages());
			model.addAttribute("pageNumbers", pageNumbers);
			
			// 검색 결과 정보
			if (hasSearchCondition) {
				model.addAttribute("isSearchResult", true);
				model.addAttribute("searchResultMessage", buildSearchResultMessage(keyword, publicStatus, createdDate, votingDate, questionPage.getTotalElements()));
			} else {
				model.addAttribute("isSearchResult", false);
			}
			
			System.out.println("=== 페이징 정보 ===");
			System.out.println("현재 페이지: " + page);
			System.out.println("페이지 크기: " + size);
			System.out.println("전체 페이지: " + questionPage.getTotalPages());
			System.out.println("전체 질문 수: " + questionPage.getTotalElements());
			System.out.println("현재 페이지 질문 수: " + questionPage.getContent().size());
			
		} catch (Exception e) {
			System.err.println("질문 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			
			// 오류 발생 시 빈 페이지 정보 설정
			model.addAttribute("questions", new ArrayList<>());
			model.addAttribute("currentPage", 0);
			model.addAttribute("totalPages", 0);
			model.addAttribute("totalElements", 0L);
			model.addAttribute("size", size);
			model.addAttribute("hasNext", false);
			model.addAttribute("hasPrevious", false);
			model.addAttribute("pageNumbers", new ArrayList<>());
			model.addAttribute("errorMessage", "질문 목록을 불러오는 중 오류가 발생했습니다.");
		}
		
		return "admin/question/list";
	}
	
	/**
	 * 페이지 번호 리스트 생성 (현재 페이지 중심으로 최대 10개)
	 */
	private List<Integer> generatePageNumbers(int currentPage, int totalPages) {
		List<Integer> pageNumbers = new ArrayList<>();
		
		if (totalPages <= 10) {
			// 총 페이지가 10개 이하면 모든 페이지 표시
			for (int i = 0; i < totalPages; i++) {
				pageNumbers.add(i);
			}
		} else {
			// 총 페이지가 10개 초과면 현재 페이지 중심으로 10개 표시
			int start = Math.max(0, currentPage - 5);
			int end = Math.min(totalPages, start + 10);
			
			// 끝에서 10개가 안 되면 시작점 조정
			if (end - start < 10) {
				start = Math.max(0, end - 10);
			}
			
			for (int i = start; i < end; i++) {
				pageNumbers.add(i);
			}
		}
		
		return pageNumbers;
	}
	
	/**
	 * 검색 결과 메시지 생성
	 */
	private String buildSearchResultMessage(String keyword, String publicStatus, LocalDate createdDate, LocalDate votingDate, long totalElements) {
		StringBuilder message = new StringBuilder();
		List<String> conditions = new ArrayList<>();
		
		if (keyword != null && !keyword.trim().isEmpty()) {
			conditions.add("키워드 '" + keyword + "'");
		}
		if (publicStatus != null && !publicStatus.trim().isEmpty()) {
			String statusText = "yes".equals(publicStatus) ? "공개" : "비공개";
			conditions.add("상태 '" + statusText + "'");
		}
		if (createdDate != null) {
			conditions.add("생성일 '" + createdDate + "'");
		}
		if (votingDate != null) {
			conditions.add("투표일 '" + votingDate + "'");
		}
		
		if (!conditions.isEmpty()) {
			message.append(String.join(", ", conditions));
			message.append("에 대한 검색 결과 총 ").append(totalElements).append("개");
		}
		
		return message.toString();
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
	                    System.out.println("사진 " + i + " - ID: " + pic.getMemberId() + ", Google Drive URL: " + pic.getGoogleDriveUrl());

	                    // 구글 드라이브 URL이 있는 사진만 추가
	                    if (pic.getGoogleDriveUrl() != null && !pic.getGoogleDriveUrl().isEmpty()) {
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
	                    } else {
	                        System.out.println("구글 드라이브 URL 없음, 건너뜀: " + pic.getMemberId());
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

	        return "admin/question/create";
	    } catch (Exception e) {
	        System.err.println("오류 발생: " + e.getMessage());
	        e.printStackTrace();
	        model.addAttribute("errorMessage", "질문 생성 중 오류가 발생했습니다: " + e.getMessage());
	        return "error";
	    }
	}

	// 질문 저장
	@PostMapping("/creat")
	public String createQuestion(@ModelAttribute QuestionEntity question,
			@RequestParam(value = "imageReference1", required = false) String imageReference1,
            @RequestParam(value = "imageReference2", required = false) String imageReference2,
            @RequestParam(value = "imageReference3", required = false) String imageReference3,
            @RequestParam(value = "imageReference4", required = false) String imageReference4) {

	    // isPublic 기본값 설정
	    if (question.getIsPublic() == null
	        || (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
	        question.setIsPublic("no");
	    }
	    
	    System.out.println("=== 컨트롤러 디버깅 ===");
	    System.out.println("imageReference1: " + imageReference1);
	    System.out.println("imageReference2: " + imageReference2);
	    System.out.println("imageReference3: " + imageReference3);
	    System.out.println("imageReference4: " + imageReference4);
	    
	    // 이미지 파일 ID를 프록시 URL로 변환해서 저장
	    if (imageReference1 != null && !imageReference1.isEmpty()) {
	        question.setOption1("/proxy/image/" + imageReference1);
	    }
	    if (imageReference2 != null && !imageReference2.isEmpty()) {
	        question.setOption2("/proxy/image/" + imageReference2);
	    }
	    if (imageReference3 != null && !imageReference3.isEmpty()) {
	        question.setOption3("/proxy/image/" + imageReference3);
	    }
	    if (imageReference4 != null && !imageReference4.isEmpty()) {
	        question.setOption4("/proxy/image/" + imageReference4);
	    }

	    System.out.println("저장될 질문: " + question);
	    questionService.createQuestion(question);

	    try {
	        String successMessage = URLEncoder.encode("새 질문이 성공적으로 생성되었습니다. (기본 상태: 비공개)",
	                StandardCharsets.UTF_8.toString());
	        return "redirect:/admin/questions?status=success&message=" + successMessage;
	    } catch (Exception e) {
	        return "redirect:/admin/questions";
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
			if (question.getIsPublic() == null
					|| (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
				question.setIsPublic("no");
			}

			model.addAttribute("question", question);
			return "admin/question/detail"; // 기존 detail.html 사용
		} catch (Exception e) {
			System.err.println("질문 조회 실패: " + e.getMessage());
			e.printStackTrace();
			try {
				String errorMessage = URLEncoder.encode("질문을 찾을 수 없습니다.", StandardCharsets.UTF_8.toString());
				return "redirect:/admin/questions?status=error&message=" + errorMessage;
			} catch (Exception ex) {
				return "redirect:/admin/questions";
			}
		}
	}

	// 질문 상세 페이지 (필요한 경우)
	@GetMapping("/{serialNumber}")
	public String getQuestion(@PathVariable("serialNumber") String serialNumber, Model model) {
		QuestionEntity question = questionService.getQuestionBySerialNumber(serialNumber);

		// 상태가 null이거나 유효하지 않은 경우 기본값으로 설정
		if (question.getIsPublic() == null
				|| (!question.getIsPublic().equals("yes") && !question.getIsPublic().equals("no"))) {
			question.setIsPublic("no");
		}

		model.addAttribute("question", question);
		return "admin/question/detail";
	}

	// 공개 상태만 변경하는 API 엔드포인트
	@PostMapping("/{serialNumber}/visibility")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> updateVisibility(@PathVariable("serialNumber") String serialNumber,
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

	// 질문 수정 처리 - 투표 기간 기능 추가 (수정된 부분)
	@PostMapping("/{serialNumber}")
	public String updateQuestion(@PathVariable("serialNumber") String serialNumber,
			@RequestParam("question") String questionText,
			@RequestParam("option1") String option1,
			@RequestParam("option2") String option2,
			@RequestParam("option3") String option3,
			@RequestParam("option4") String option4,
			@RequestParam("isPublic") String isPublic,
			@RequestParam(value = "votingStartDate", required = false) String votingStartDateStr,
			@RequestParam(value = "votingEndDate", required = false) String votingEndDateStr) {
		try {
			System.out.println("=== 질문 수정 요청 ===");
			System.out.println("일련번호: " + serialNumber);
			System.out.println("공개상태: " + isPublic);
			System.out.println("투표시작일: " + votingStartDateStr);
			System.out.println("투표종료일: " + votingEndDateStr);

			// 기존 질문 가져오기
			QuestionEntity existingQuestion = questionService.getQuestionBySerialNumber(serialNumber);

			// 폼에서 입력받은 값을 기존 질문에 복사
			existingQuestion.setQuestion(questionText);
			existingQuestion.setOption1(option1);
			existingQuestion.setOption2(option2);
			existingQuestion.setOption3(option3);
			existingQuestion.setOption4(option4);

			// 공개 상태 처리
			if (isPublic == null || (!isPublic.equals("yes") && !isPublic.equals("no"))) {
				isPublic = "no";
			}

			// 투표 기간 처리
			LocalDateTime votingStartDate = null;
			LocalDateTime votingEndDate = null;
			
			if (votingStartDateStr != null && !votingStartDateStr.trim().isEmpty()) {
				try {
					LocalDate startDate = LocalDate.parse(votingStartDateStr);
					votingStartDate = startDate.atTime(8, 0); // 08:00으로 설정
					System.out.println("파싱된 투표시작일: " + votingStartDate);
				} catch (Exception e) {
					System.err.println("투표 시작일 파싱 오류: " + e.getMessage());
				}
			}
			
			if (votingEndDateStr != null && !votingEndDateStr.trim().isEmpty()) {
				try {
					LocalDate endDate = LocalDate.parse(votingEndDateStr);
					votingEndDate = endDate.atTime(22, 0); // 22:00으로 설정
					System.out.println("파싱된 투표종료일: " + votingEndDate);
				} catch (Exception e) {
					System.err.println("투표 종료일 파싱 오류: " + e.getMessage());
				}
			}

			// 유효성 검사
			String validationResult = validateQuestionUpdate(isPublic, votingStartDate, votingEndDate);
			if (validationResult != null) {
				String errorMessage = URLEncoder.encode(validationResult, StandardCharsets.UTF_8.toString());
				return "redirect:/admin/questions/" + serialNumber + "/edit?status=error&message=" + errorMessage;
			}

			// 공개 상태 변경 여부 확인
			boolean isPublicChanged = !existingQuestion.getIsPublic().equals(isPublic);

			// 값 설정
			existingQuestion.setIsPublic(isPublic);
			existingQuestion.setVotingStartDate(votingStartDate);
			existingQuestion.setVotingEndDate(votingEndDate);
			
			// 투표 상태 설정 (공개이고 투표 기간이 설정된 경우에만 '진행중'으로 설정)
			if ("yes".equals(isPublic) && votingStartDate != null && votingEndDate != null) {
				existingQuestion.setVotingStatus("진행중");
			} else {
				existingQuestion.setVotingStatus(null); // 비공개이거나 투표 기간 없으면 null
			}

			System.out.println("저장할 질문 정보: " + existingQuestion);

			// 수정된 기존 질문 저장
			questionService.updateQuestion(existingQuestion);

			// 성공 메시지 구성
			String successMessage = buildSuccessMessage(serialNumber, isPublicChanged, isPublic, votingStartDate, votingEndDate);

			return "redirect:/admin/questions?status=success&message="
					+ URLEncoder.encode(successMessage, StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				String errorMessage = URLEncoder.encode("질문 수정 중 오류가 발생했습니다: " + e.getMessage(), StandardCharsets.UTF_8.toString());
				return "redirect:/admin/questions?status=error&message=" + errorMessage;
			} catch (Exception ex) {
				return "redirect:/error";
			}
		}
	}

	/**
	 * 질문 수정 유효성 검사
	 */
	private String validateQuestionUpdate(String isPublic, LocalDateTime votingStartDate, LocalDateTime votingEndDate) {
		// 공개 상태이면서 투표 기간이 설정되지 않은 경우
		if ("yes".equals(isPublic) && (votingStartDate == null || votingEndDate == null)) {
			return "공개 질문으로 설정하려면 투표 시작일과 종료일을 모두 입력해야 합니다.";
		}
		
		// 비공개 상태이면서 투표 기간만 설정된 경우
		if ("no".equals(isPublic) && (votingStartDate != null || votingEndDate != null)) {
			return "비공개 질문에는 투표 기간을 설정할 수 없습니다. 먼저 공개로 변경하세요.";
		}
		
		// 투표 기간이 설정된 경우 시작일과 종료일 검증
		if (votingStartDate != null && votingEndDate != null) {
			if (votingStartDate.isAfter(votingEndDate)) {
				return "투표 시작일은 종료일보다 이전이어야 합니다.";
			}
			
			if (votingStartDate.isBefore(LocalDateTime.now().minusDays(1))) {
				return "투표 시작일은 과거 날짜로 설정할 수 없습니다.";
			}
		}
		
		return null; // 유효성 검사 통과
	}

	/**
	 * 성공 메시지 구성
	 */
	private String buildSuccessMessage(String serialNumber, boolean isPublicChanged, String isPublic, 
	                                  LocalDateTime votingStartDate, LocalDateTime votingEndDate) {
		StringBuilder message = new StringBuilder();
		message.append(String.format("질문 %s이(가) 성공적으로 수정되었습니다.", serialNumber));
		
		if (isPublicChanged) {
			if ("yes".equals(isPublic)) {
				message.append(" 비공개에서 공개 상태로 변경되었습니다.");
			} else {
				message.append(" 공개에서 비공개 상태로 변경되었습니다.");
			}
		}
		
		if (votingStartDate != null && votingEndDate != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			message.append(String.format(" 투표 기간: %s 08:00 ~ %s 22:00", 
				votingStartDate.format(formatter), 
				votingEndDate.format(formatter)));
		}
		
		return message.toString();
	}

	// 질문 삭제 처리
	@GetMapping("/{serialNumber}/delete")
	public String deleteQuestion(@PathVariable("serialNumber") String serialNumber) {
		try {
			System.out.println("삭제 요청 받음: " + serialNumber);
			questionService.deleteQuestion(serialNumber);
			System.out.println("삭제 완료: " + serialNumber);

			String successMessage = URLEncoder.encode("질문이 성공적으로 삭제되었습니다.", StandardCharsets.UTF_8.toString());
			return "redirect:/admin/questions?status=success&message=" + successMessage;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("삭제 실패: " + e.getMessage());

			try {
				String errorMessage = URLEncoder.encode("질문 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8.toString());
				return "redirect:/admin/questions?status=error&message=" + errorMessage;
			} catch (Exception ex) {
				return "redirect:/error";
			}
		}
	}
}