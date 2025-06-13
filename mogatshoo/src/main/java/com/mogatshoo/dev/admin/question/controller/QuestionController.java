package com.mogatshoo.dev.admin.question.controller;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.scheduler.VotingStatusScheduler;
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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/questions")
public class QuestionController {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private HairLossTestService pictureService;

	@Autowired
	private MemberService memberService;
	
	@Autowired
	private VotingStatusScheduler votingStatusScheduler;

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
	        
	        System.out.println("memberId : " + memberId);
	        member = memberService.findByMemberId(memberId);
	        model.addAttribute("member", member);
	        
	        // 랜덤으로 4개의 사진 가져오기
	        List<PictureEntity> fetchedPictures = pictureService.getRandomPictures(4);
	        List<PictureEntity> randomPictures = new ArrayList<>();
	        
	        // 사진과 연결된 회원 정보를 담을 맵
	        Map<String, String> memberNicknames = new HashMap<>();
	        
	        System.out.println("📸 ===== 질문 생성 - 보기 회원 정보 (개선된 버전) =====");
	        System.out.println("질문 번호: " + nextSerialNumber);
	        System.out.println("요청된 사진 수: 4개");
	        
	        if (fetchedPictures != null) {
	            System.out.println("실제 가져온 사진 수: " + fetchedPictures.size());

	            for (int i = 0; i < fetchedPictures.size(); i++) {
	                PictureEntity pic = fetchedPictures.get(i);
	                if (pic != null) {
	                    System.out.println("--- Option" + (i+1) + " 정보 (개선된 조회) ---");
	                    System.out.println("회원 ID: " + pic.getMemberId());
	                    System.out.println("Firebase Storage URL: " + pic.getFirebaseStorageUrl());

	                    // Firebase Storage URL이 있는 사진만 추가
	                    if (pic.getFirebaseStorageUrl() != null && !pic.getFirebaseStorageUrl().isEmpty()) {
	                        randomPictures.add(pic);
	                        
	                        // ===== 개선된 회원 ID 조회 시작 =====
	                        try {
	                            MemberEntity picMember = null;
	                            String originalMemberId = pic.getMemberId();
	                            
	                            System.out.println("🔍 회원 조회 시작: " + originalMemberId);
	                            
	                            // 1. 기본 회원 조회
	                            try {
	                                picMember = memberService.findByMemberId(originalMemberId);
	                                if (picMember != null) {
	                                    System.out.println("✅ 기본 조회 성공: " + picMember.getMemberName());
	                                } else {
	                                    System.out.println("⚠️ 기본 조회 실패, 대안 조회 시도...");
	                                }
	                            } catch (Exception e) {
	                                System.out.println("❌ 기본 조회 중 예외: " + e.getMessage());
	                            }
	                            
	                            // 2. 기본 조회 실패 시 Provider별 조회 시도
	                            if (picMember == null) {
	                                System.out.println("🔄 Provider별 조회 시도...");
	                                
	                                // Google 패턴 체크
	                                if (originalMemberId.startsWith("google_")) {
	                                    try {
	                                        String providerId = originalMemberId.substring(7); // "google_" 제거
	                                        System.out.println("Google providerId 추출: " + providerId);
	                                        
	                                        picMember = memberService.findByProviderAndProviderId("google", providerId);
	                                        if (picMember != null) {
	                                            System.out.println("✅ Google Provider 조회 성공: " + picMember.getMemberName());
	                                            System.out.println("실제 DB memberId: " + picMember.getMemberId());
	                                        }
	                                    } catch (Exception e) {
	                                        System.out.println("❌ Google Provider 조회 실패: " + e.getMessage());
	                                    }
	                                }
	                                
	                                // Naver 패턴 체크
	                                else if (originalMemberId.startsWith("naver_")) {
	                                    try {
	                                        String providerId = originalMemberId.substring(6); // "naver_" 제거
	                                        System.out.println("Naver providerId 추출: " + providerId);
	                                        
	                                        picMember = memberService.findByProviderAndProviderId("naver", providerId);
	                                        if (picMember != null) {
	                                            System.out.println("✅ Naver Provider 조회 성공: " + picMember.getMemberName());
	                                            System.out.println("실제 DB memberId: " + picMember.getMemberId());
	                                        }
	                                    } catch (Exception e) {
	                                        System.out.println("❌ Naver Provider 조회 실패: " + e.getMessage());
	                                    }
	                                }
	                                
	                                // Kakao 패턴 체크
	                                else if (originalMemberId.startsWith("kakao_")) {
	                                    try {
	                                        String providerId = originalMemberId.substring(6); // "kakao_" 제거
	                                        System.out.println("Kakao providerId 추출: " + providerId);
	                                        
	                                        picMember = memberService.findByProviderAndProviderId("kakao", providerId);
	                                        if (picMember != null) {
	                                            System.out.println("✅ Kakao Provider 조회 성공: " + picMember.getMemberName());
	                                            System.out.println("실제 DB memberId: " + picMember.getMemberId());
	                                        }
	                                    } catch (Exception e) {
	                                        System.out.println("❌ Kakao Provider 조회 실패: " + e.getMessage());
	                                    }
	                                }
	                            }
	                            
	                            // 3. 회원 정보 처리
	                            if (picMember != null) {
	                                // 닉네임 맵에 추가 (원래 PictureEntity의 memberId를 키로 사용)
	                                memberNicknames.put(originalMemberId, picMember.getMemberName());
	                                
	                                // 상세 회원 정보 콘솔 출력
	                                System.out.println("🎯 === 최종 회원 정보 ===");
	                                System.out.println("Picture의 memberId: " + originalMemberId);
	                                System.out.println("실제 DB memberId: " + picMember.getMemberId());
	                                System.out.println("실명: " + picMember.getMemberName());
	                                System.out.println("닉네임: " + picMember.getMemberNickName());
	                                System.out.println("이메일: " + picMember.getMemberEmail());
	                                System.out.println("성별: " + picMember.getMemberGender());
	                                System.out.println("전화번호: " + picMember.getMemberTel());
	                                System.out.println("가입일: " + picMember.getMemberCreate());
	                                System.out.println("Provider: " + picMember.getProvider());
	                                System.out.println("Provider ID: " + picMember.getProviderId());
	                                
	                                // QuestionEntity에 회원 ID 설정 (원래 PictureEntity의 memberId 사용)
	                                switch (i) {
	                                    case 0:
	                                        question.setOption1MemberId(originalMemberId);
	                                        System.out.println("✅ Option1MemberId 설정됨: " + originalMemberId);
	                                        break;
	                                    case 1:
	                                        question.setOption2MemberId(originalMemberId);
	                                        System.out.println("✅ Option2MemberId 설정됨: " + originalMemberId);
	                                        break;
	                                    case 2:
	                                        question.setOption3MemberId(originalMemberId);
	                                        System.out.println("✅ Option3MemberId 설정됨: " + originalMemberId);
	                                        break;
	                                    case 3:
	                                        question.setOption4MemberId(originalMemberId);
	                                        System.out.println("✅ Option4MemberId 설정됨: " + originalMemberId);
	                                        break;
	                                }
	                                
	                            } else {
	                                // 회원 정보를 찾을 수 없는 경우
	                                memberNicknames.put(originalMemberId, "❌ 알 수 없는 회원");
	                                System.out.println("❌ 회원 정보 조회 완전 실패");
	                                System.out.println("PictureEntity의 memberId: " + originalMemberId);
	                                
	                                // 디버깅을 위한 전체 회원 정보 출력
	                                System.out.println("🔍 === 디버깅: 전체 SNS 회원 목록 ===");
	                                try {
	                                    // 모든 회원 조회해서 SNS 회원들만 출력
	                                    List<MemberEntity> allMembers = memberService.getAllMembers();
	                                    if (allMembers != null) {
	                                        for (MemberEntity debugMember : allMembers) {
	                                            if (!debugMember.getProvider().equals("local")) {
	                                                System.out.println("- DB회원ID: " + debugMember.getMemberId() + 
	                                                                 " | Provider: " + debugMember.getProvider() + 
	                                                                 " | ProviderID: " + debugMember.getProviderId() + 
	                                                                 " | 이름: " + debugMember.getMemberName());
	                                            }
	                                        }
	                                    }
	                                } catch (Exception debugE) {
	                                    System.out.println("디버깅 정보 출력 실패: " + debugE.getMessage());
	                                }
	                                System.out.println("=================================");
	                            }
	                            
	                        } catch (Exception e) {
	                            System.err.println("❌ 회원 정보 조회 중 예외 발생 [memberId: " + pic.getMemberId() + "]: " + e.getMessage());
	                            e.printStackTrace();
	                            memberNicknames.put(pic.getMemberId(), "❌ 조회 오류");
	                            System.out.println("회원 정보: 조회 중 오류 발생");
	                        }
	                    } else {
	                        System.out.println("Firebase Storage URL 없음, 건너뜀");
	                    }
	                    System.out.println("--------------------------------");
	                } else {
	                    System.out.println("--- Option" + (i+1) + " 정보 ---");
	                    System.out.println("사진 데이터: null");
	                    System.out.println("--------------------------------");
	                }
	            }

	            System.out.println("=== 최종 선정된 보기 정보 ===");
	            System.out.println("유효한 사진 수: " + randomPictures.size());
	            
	            if (randomPictures.size() >= 2) {
	                System.out.println("✅ 질문 생성 가능 (최소 2개 옵션 확보)");
	                
	                // 최종 선정된 보기들의 요약 정보
	                for (int i = 0; i < randomPictures.size(); i++) {
	                    PictureEntity pic = randomPictures.get(i);
	                    String memberName = memberNicknames.get(pic.getMemberId());
	                    System.out.println(String.format("Option%d: %s (%s)", 
	                                                    i+1, 
	                                                    memberName != null ? memberName : "알 수 없음", 
	                                                    pic.getMemberId()));
	                }
	            } else {
	                System.out.println("❌ 질문 생성 불가 (최소 2개 옵션 필요)");
	            }
	            System.out.println("========================================");
	            
	        } else {
	            System.out.println("가져온 사진 목록이 null입니다");
	            System.out.println("========================================");
	        }

	        model.addAttribute("question", question);
	        model.addAttribute("randomPictures", randomPictures);
	        model.addAttribute("memberNicknames", memberNicknames);

	        System.out.println("모델에 추가된 사진 목록 크기: " + randomPictures.size());
	        System.out.println("모델에 추가된 닉네임 정보 크기: " + memberNicknames.size());

	        return "admin/question/create";
	        
	    } catch (Exception e) {
	        System.err.println("질문 생성 폼 로딩 중 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	        model.addAttribute("errorMessage", "질문 생성 중 오류가 발생했습니다: " + e.getMessage());
	        return "error";
	    }
	}

	/**
	 * Firebase Storage URL에서 회원 ID 추출하는 메서드 (개선된 버전)
	 * URL 패턴: https://firebasestorage.googleapis.com/.../member%2F{memberId}%2F...
	 */
	private String extractMemberIdFromFirebaseUrl(String firebaseUrl) {
	    System.out.println("🔍 === Firebase URL에서 회원 ID 추출 시작 ===");
	    System.out.println("원본 URL: " + firebaseUrl);
	    
	    if (firebaseUrl == null || firebaseUrl.isEmpty()) {
	        System.out.println("❌ URL이 null이거나 빈 문자열");
	        return null;
	    }
	    
	    try {
	        // 방법 1: 개선된 정규식 사용 (더 유연한 패턴)
	        String pattern1 = "member%2F([^%2F]+?)%2F";
	        Pattern p1 = Pattern.compile(pattern1);
	        Matcher m1 = p1.matcher(firebaseUrl);
	        
	        if (m1.find()) {
	            String memberId = URLDecoder.decode(m1.group(1), StandardCharsets.UTF_8);
	            System.out.println("✅ 방법1 성공 - 추출된 회원 ID: " + memberId);
	            return memberId;
	        }
	        
	        System.out.println("⚠️ 방법1 실패, 방법2 시도...");
	        
	        // 방법 2: 문자열 분할 방식 (더 안전한 방법)
	        try {
	            // URL을 디코딩
	            String decodedUrl = URLDecoder.decode(firebaseUrl, StandardCharsets.UTF_8);
	            System.out.println("디코딩된 URL: " + decodedUrl);
	            
	            // "member/" 문자열 찾기
	            String memberPrefix = "member/";
	            int memberIndex = decodedUrl.indexOf(memberPrefix);
	            
	            if (memberIndex != -1) {
	                // "member/" 다음 위치부터 시작
	                int startIndex = memberIndex + memberPrefix.length();
	                
	                // 다음 "/" 위치 찾기
	                int endIndex = decodedUrl.indexOf("/", startIndex);
	                
	                if (endIndex != -1) {
	                    String memberId = decodedUrl.substring(startIndex, endIndex);
	                    System.out.println("✅ 방법2 성공 - 추출된 회원 ID: " + memberId);
	                    return memberId;
	                }
	            }
	            
	            System.out.println("⚠️ 방법2 실패, 방법3 시도...");
	            
	        } catch (Exception e2) {
	            System.out.println("❌ 방법2 중 예외: " + e2.getMessage());
	        }
	        
	        // 방법 3: 가장 관대한 정규식 (모든 문자 허용)
	        String pattern3 = "member%2F(.+?)%2F";
	        Pattern p3 = Pattern.compile(pattern3);
	        Matcher m3 = p3.matcher(firebaseUrl);
	        
	        if (m3.find()) {
	            String memberId = URLDecoder.decode(m3.group(1), StandardCharsets.UTF_8);
	            System.out.println("✅ 방법3 성공 - 추출된 회원 ID: " + memberId);
	            return memberId;
	        }
	        
	        System.out.println("❌ 모든 방법 실패");
	        
	        // 방법 4: 디버깅을 위한 URL 구조 분석
	        System.out.println("🔍 === URL 구조 분석 (디버깅) ===");
	        String[] parts = firebaseUrl.split("%2F");
	        System.out.println("URL 분할 결과:");
	        for (int i = 0; i < parts.length; i++) {
	            System.out.println("  [" + i + "]: " + parts[i]);
	        }
	        
	        // "member" 다음에 오는 부분 찾기
	        for (int i = 0; i < parts.length - 1; i++) {
	            if (parts[i].endsWith("member")) {
	                String potentialMemberId = parts[i + 1];
	                if (potentialMemberId != null && !potentialMemberId.isEmpty()) {
	                    try {
	                        String decodedMemberId = URLDecoder.decode(potentialMemberId, StandardCharsets.UTF_8);
	                        System.out.println("✅ 방법4 성공 - 추출된 회원 ID: " + decodedMemberId);
	                        return decodedMemberId;
	                    } catch (Exception e4) {
	                        System.out.println("❌ 방법4 디코딩 실패: " + e4.getMessage());
	                    }
	                }
	            }
	        }
	        
	    } catch (Exception e) {
	        System.err.println("❌ 회원 ID 추출 중 전체 오류: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    System.out.println("❌ 회원 ID 추출 완전 실패");
	    return null;
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
	    
	    System.out.println("=== 💾 질문 저장 시작 ===");
	    System.out.println("일련번호: " + question.getSerialNumber());
	    System.out.println("질문 내용: " + question.getQuestion());
	    System.out.println("공개 상태: " + question.getIsPublic());

	    // Firebase Storage URL 저장 및 회원 ID 자동 추출
	    if (imageReference1 != null && !imageReference1.isEmpty()) {
	        question.setOption1(imageReference1);
	        String memberId1 = extractMemberIdFromFirebaseUrl(imageReference1);
	        question.setOption1MemberId(memberId1);
	        System.out.println("✅ Option1 - 이미지: " + imageReference1.substring(0, Math.min(50, imageReference1.length())) + "...");
	        System.out.println("✅ Option1 - 추출된 회원 ID: " + memberId1);
	    }
	    
	    if (imageReference2 != null && !imageReference2.isEmpty()) {
	        question.setOption2(imageReference2);
	        String memberId2 = extractMemberIdFromFirebaseUrl(imageReference2);
	        question.setOption2MemberId(memberId2);
	        System.out.println("✅ Option2 - 이미지: " + imageReference2.substring(0, Math.min(50, imageReference2.length())) + "...");
	        System.out.println("✅ Option2 - 추출된 회원 ID: " + memberId2);
	    }
	    
	    if (imageReference3 != null && !imageReference3.isEmpty()) {
	        question.setOption3(imageReference3);
	        String memberId3 = extractMemberIdFromFirebaseUrl(imageReference3);
	        question.setOption3MemberId(memberId3);
	        System.out.println("✅ Option3 - 이미지: " + imageReference3.substring(0, Math.min(50, imageReference3.length())) + "...");
	        System.out.println("✅ Option3 - 추출된 회원 ID: " + memberId3);
	    }
	    
	    if (imageReference4 != null && !imageReference4.isEmpty()) {
	        question.setOption4(imageReference4);
	        String memberId4 = extractMemberIdFromFirebaseUrl(imageReference4);
	        question.setOption4MemberId(memberId4);
	        System.out.println("✅ Option4 - 이미지: " + imageReference4.substring(0, Math.min(50, imageReference4.length())) + "...");
	        System.out.println("✅ Option4 - 추출된 회원 ID: " + memberId4);
	    }

	    // 추출된 회원 ID 요약
	    System.out.println("\n🎯 === 추출된 회원 ID 요약 ===");
	    System.out.println("Option1 회원 ID: " + question.getOption1MemberId());
	    System.out.println("Option2 회원 ID: " + question.getOption2MemberId());
	    System.out.println("Option3 회원 ID: " + question.getOption3MemberId());
	    System.out.println("Option4 회원 ID: " + question.getOption4MemberId());

	    // 질문 저장
	    QuestionEntity savedQuestion = questionService.createQuestion(question);
	    
	    // 저장 후 검증 및 상세 회원 정보 출력
	    System.out.println("\n🔍 === 저장 완료 후 회원 정보 검증 ===");
	    QuestionEntity verificationQuestion = questionService.getQuestionBySerialNumber(savedQuestion.getSerialNumber());
	    
	    // 각 옵션별 회원 정보 상세 출력
	    printDetailedMemberInfo(verificationQuestion);
	    
	    System.out.println("========================================");
	    System.out.println("✅ 질문 생성 및 회원 정보 연결 완료!");
	    System.out.println("========================================\n");

	    try {
	        String successMessage = URLEncoder.encode("새 질문이 성공적으로 생성되었습니다. (회원 정보 연결 완료)",
	                StandardCharsets.UTF_8.toString());
	        return "redirect:/admin/questions?status=success&message=" + successMessage;
	    } catch (Exception e) {
	        return "redirect:/admin/questions";
	    }
	}

	/**
	 * 질문의 모든 옵션에 대한 상세 회원 정보 출력
	 */
	private void printDetailedMemberInfo(QuestionEntity question) {
	    System.out.println("질문 번호: " + question.getSerialNumber());
	    System.out.println("질문 내용: " + question.getQuestion());
	    System.out.println("생성 시간: " + question.getCreatedAt());
	    System.out.println("");
	    
	    // Option별 회원 정보 출력
	    if (question.getOption1MemberId() != null) {
	        printMemberDetailsForVoting("Option1", question.getOption1MemberId(), question.getOption1());
	    }
	    if (question.getOption2MemberId() != null) {
	        printMemberDetailsForVoting("Option2", question.getOption2MemberId(), question.getOption2());
	    }
	    if (question.getOption3MemberId() != null) {
	        printMemberDetailsForVoting("Option3", question.getOption3MemberId(), question.getOption3());
	    }
	    if (question.getOption4MemberId() != null) {
	        printMemberDetailsForVoting("Option4", question.getOption4MemberId(), question.getOption4());
	    }
	}

	/**
	 * 투표 시스템용 회원 상세 정보 출력 (이메일 발송 준비)
	 */
	private void printMemberDetailsForVoting(String optionName, String memberId, String imageUrl) {
	    try {
	        System.out.println("🏆 === " + optionName + " 투표 대상 정보 ===");
	        System.out.println("회원 ID: " + memberId);
	        System.out.println("이미지 URL: " + imageUrl);
	        
	        // 회원 정보 조회
	        MemberEntity member = memberService.findByMemberId(memberId);
	        if (member != null) {
	            System.out.println("📧 수상자 이메일: " + member.getMemberEmail());
	            System.out.println("📛 실명: " + member.getMemberName());
	            System.out.println("🏷️ 닉네임: " + member.getMemberNickName());
	            System.out.println("⚧️ 성별: " + member.getMemberGender());
	            System.out.println("📱 전화번호: " + member.getMemberTel());
	            System.out.println("🎂 생년월일: " + member.getMemberBirth());
	            System.out.println("📅 가입일: " + member.getMemberCreate());
	            System.out.println("🌐 로그인 방식: " + member.getProvider());
	            System.out.println("🔑 권한: " + member.getRole());
	            System.out.println("📍 주소: " + member.getMemberZipcode() + " " + 
	                             member.getMemberAddress1() + " " + member.getMemberAddress2());
	        } else {
	            System.out.println("❌ 회원 정보를 찾을 수 없습니다. (탈퇴한 회원일 수 있음)");
	        }
	        System.out.println("================================================");
	    } catch (Exception e) {
	        System.err.println("❌ " + optionName + " 회원 정보 조회 실패: " + e.getMessage());
	        System.out.println("================================================");
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
	
	@PostMapping("/archive")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> manualArchive() {
	    Map<String, Object> response = new HashMap<>();
	    
	    try {
	        votingStatusScheduler.manualArchive();
	        
	        response.put("success", true);
	        response.put("message", "질문 아카이빙이 완료되었습니다.");
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        System.err.println("수동 아카이빙 실패: " + e.getMessage());
	        e.printStackTrace();
	        
	        response.put("success", false);
	        response.put("message", "아카이빙 중 오류가 발생했습니다: " + e.getMessage());
	        return ResponseEntity.badRequest().body(response);
	    }
	}
}