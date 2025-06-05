package com.mogatshoo.dev.voting.controller;

import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import com.mogatshoo.dev.admin.question.entity.QuestionEntity;
import com.mogatshoo.dev.admin.question.service.QuestionService;
import com.mogatshoo.dev.voting.entity.VotingEntity;
import com.mogatshoo.dev.voting.service.VotingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/voting")
public class VotingController {

	@Autowired
	private VotingService votingService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private MemberService memberService;

	// 투표 페이지 로드
	@GetMapping({ "", "/voting" })
	public String votingPage(Model model, RedirectAttributes redirectAttributes) {
		try {
			// 현재 로그인한 사용자 ID 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String currentMemberId = authentication.getName();

			// 로그인 체크
			if (currentMemberId == null || currentMemberId.trim().isEmpty()
					|| "anonymousUser".equals(currentMemberId)) {
				redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
				return "redirect:/member/login";
			}

			// 사용자 정보 조회
			MemberEntity member = memberService.findByMemberId(currentMemberId);
			if (member == null) {
				redirectAttributes.addFlashAttribute("message", "사용자 정보를 찾을 수 없습니다.");
				return "redirect:/member/login";
			}

			// ADMIN 권한 체크 - 투표 불가
			if ("ADMIN".equals(member.getRole())) {
				redirectAttributes.addFlashAttribute("message", "관리자는 투표에 참여할 수 없습니다.");
				return "redirect:/"; // 메인 페이지로 리다이렉트
			}

			// USER만 투표 가능
			if (!"USER".equals(member.getRole())) {
				redirectAttributes.addFlashAttribute("message", "투표 권한이 없습니다.");
				return "redirect:/";
			}

			model.addAttribute("member", member);

			// 투표하지 않은 랜덤 질문 가져오기
			QuestionEntity randomQuestion = votingService.getRandomUnansweredQuestion(currentMemberId);

			if (randomQuestion == null) {
				model.addAttribute("noMoreQuestions", true);
				model.addAttribute("message", "현재 투표할 질문이 없습니다.");
				return "voting/voting";
			}
			
			// 투표 가능 여부 체크
	        if (!randomQuestion.isVotingAvailable()) {
	            String status = randomQuestion.getCurrentVotingStatus();
	            String message;
	            
	            switch (status) {
	                case "보류":
	                    message = "아직 투표가 시작되지 않았습니다. " + randomQuestion.getVotingStatusSummary();
	                    break;
	                case "종료":
	                    message = "투표가 종료되었습니다. " + randomQuestion.getVotingStatusSummary();
	                    break;
	                default:
	                    message = "현재 투표할 수 없는 상태입니다.";
	            }
	            
	            model.addAttribute("noMoreQuestions", true);
	            model.addAttribute("message", message);
	            return "voting/voting";
	        }

			// 질문의 원본 옵션 이미지들을 가져와서 리스트로 구성
			List<Map<String, String>> questionOptions = new ArrayList<>();
			
			// Option1
			if (randomQuestion.getOption1() != null && !randomQuestion.getOption1().isEmpty()) {
				Map<String, String> option1 = new HashMap<>();
				option1.put("imageUrl", randomQuestion.getOption1());
				option1.put("optionId", "option1");
				questionOptions.add(option1);
			}
			
			// Option2
			if (randomQuestion.getOption2() != null && !randomQuestion.getOption2().isEmpty()) {
				Map<String, String> option2 = new HashMap<>();
				option2.put("imageUrl", randomQuestion.getOption2());
				option2.put("optionId", "option2");
				questionOptions.add(option2);
			}
			
			// Option3
			if (randomQuestion.getOption3() != null && !randomQuestion.getOption3().isEmpty()) {
				Map<String, String> option3 = new HashMap<>();
				option3.put("imageUrl", randomQuestion.getOption3());
				option3.put("optionId", "option3");
				questionOptions.add(option3);
			}
			
			// Option4
			if (randomQuestion.getOption4() != null && !randomQuestion.getOption4().isEmpty()) {
				Map<String, String> option4 = new HashMap<>();
				option4.put("imageUrl", randomQuestion.getOption4());
				option4.put("optionId", "option4");
				questionOptions.add(option4);
			}

			// 최소 2개 이상의 옵션이 있는지 확인
			if (questionOptions.size() < 2) {
				model.addAttribute("noMoreQuestions", true);
				model.addAttribute("message", "현재 질문의 옵션이 부족합니다. (최소 2개 필요)");
				return "voting/voting";
			}

			model.addAttribute("question", randomQuestion);
			model.addAttribute("questionOptions", questionOptions);

			System.out.println("투표 페이지 로드 완료 - 질문: " + randomQuestion.getSerialNumber() + ", 옵션 수: " + questionOptions.size());

			return "voting/voting";
		} catch (Exception e) {
			System.err.println("투표 페이지 로딩 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("errorMessage", "투표 시스템 접근 중 오류가 발생했습니다: " + e.getMessage());
			model.addAttribute("noMoreQuestions", true);
			return "voting/voting";
		}
	}

	/**
	 * 다음 질문을 AJAX로 가져오는 API
	 */
	@GetMapping("/next-question")
	@ResponseBody
	public ResponseEntity<?> getNextQuestion() {
		Map<String, Object> response = new HashMap<>();
		
		try {
			// 현재 로그인한 사용자 ID 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String currentMemberId = authentication.getName();

			// 로그인 체크
			if (currentMemberId == null || currentMemberId.trim().isEmpty()
					|| "anonymousUser".equals(currentMemberId)) {
				response.put("success", false);
				response.put("error", "로그인이 필요합니다.");
				return ResponseEntity.status(401).body(response);
			}

			// 투표하지 않은 랜덤 질문 가져오기
			QuestionEntity nextQuestion = votingService.getRandomUnansweredQuestion(currentMemberId);

			if (nextQuestion == null) {
				response.put("success", false);
				response.put("noMoreQuestions", true);
				response.put("message", "모든 질문에 투표를 완료했습니다!");
				return ResponseEntity.ok(response);
			}

			// 투표 가능 여부 체크
			if (!nextQuestion.isVotingAvailable()) {
				String status = nextQuestion.getCurrentVotingStatus();
				String message;
				
				switch (status) {
					case "보류":
						message = "아직 투표가 시작되지 않았습니다. " + nextQuestion.getVotingStatusSummary();
						break;
					case "종료":
						message = "투표가 종료되었습니다. " + nextQuestion.getVotingStatusSummary();
						break;
					default:
						message = "현재 투표할 수 없는 상태입니다.";
				}
				
				response.put("success", false);
				response.put("noMoreQuestions", true);
				response.put("message", message);
				return ResponseEntity.ok(response);
			}

			// 질문의 원본 옵션 이미지들을 가져와서 리스트로 구성
			List<Map<String, String>> questionOptions = new ArrayList<>();
			
			// Option1~4 처리
			String[] options = {nextQuestion.getOption1(), nextQuestion.getOption2(), 
							   nextQuestion.getOption3(), nextQuestion.getOption4()};
			String[] optionIds = {"option1", "option2", "option3", "option4"};
			
			for (int i = 0; i < options.length; i++) {
				if (options[i] != null && !options[i].isEmpty()) {
					Map<String, String> option = new HashMap<>();
					option.put("imageUrl", options[i]);
					option.put("optionId", optionIds[i]);
					questionOptions.add(option);
				}
			}

			// 최소 2개 이상의 옵션이 있는지 확인
			if (questionOptions.size() < 2) {
				response.put("success", false);
				response.put("noMoreQuestions", true);
				response.put("message", "현재 질문의 옵션이 부족합니다.");
				return ResponseEntity.ok(response);
			}

			// 성공 응답
			response.put("success", true);
			response.put("question", Map.of(
				"serialNumber", nextQuestion.getSerialNumber(),
				"question", nextQuestion.getQuestion()
			));
			response.put("questionOptions", questionOptions);

			System.out.println("다음 질문 로드 완료 - 질문: " + nextQuestion.getSerialNumber() + ", 옵션 수: " + questionOptions.size());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("다음 질문 로드 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("error", "다음 질문을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 투표 제출 처리 (AJAX용)
	 */
	@PostMapping("/submit")
	@ResponseBody
	public ResponseEntity<?> submitVote(@RequestParam("serialNumber") String serialNumber,
			@RequestParam("votedId") String votedId) {

		Map<String, Object> response = new HashMap<>();

		try {
			// 현재 로그인한 사용자 정보 가져오기
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String voterId = authentication.getName();

			// 로그인 체크
			if (voterId == null || voterId.isEmpty() || "anonymousUser".equals(voterId)) {
				response.put("success", false);
				response.put("error", "로그인이 필요합니다.");
				return ResponseEntity.status(401).body(response);
			}

			System.out.println("투표 처리 시작 - 질문: " + serialNumber + ", 투표자: " + voterId + ", 선택: " + votedId);

			// 중복 투표 체크
			if (votingService.hasVoted(serialNumber, voterId)) {
				response.put("success", false);
				response.put("error", "이미 이 질문에 투표하셨습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// 투표 저장
			VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);

			if (vote != null && vote.getId() != null) {
				System.out.println("투표 저장 성공!");
				response.put("success", true);
				response.put("message", "투표가 성공적으로 저장되었습니다.");
				response.put("voteId", vote.getId());

				// 다음 질문이 있는지 확인
				QuestionEntity nextQuestion = votingService.getRandomUnansweredQuestion(voterId);
				if (nextQuestion == null) {
					response.put("noMoreQuestions", true);
					response.put("completionMessage", "모든 질문에 투표를 완료했습니다!");
				} else {
					response.put("hasNextQuestion", true);
				}

				return ResponseEntity.ok(response);
			} else {
				response.put("success", false);
				response.put("error", "투표 저장에 실패했습니다.");
				return ResponseEntity.badRequest().body(response);
			}

		} catch (Exception e) {
			System.err.println("투표 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("error", "투표 처리 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 일반 폼 제출용 (백업)
	 */
	@PostMapping("/submit-form")
	public String submitVoteForm(@RequestParam("serialNumber") String serialNumber,
			@RequestParam("votedId") String votedId, RedirectAttributes redirectAttributes) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String voterId = authentication.getName();

			if (voterId == null || voterId.isEmpty() || "anonymousUser".equals(voterId)) {
				redirectAttributes.addFlashAttribute("voteError", true);
				redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보를 찾을 수 없습니다.");
				return "redirect:/member/login";
			}

			// 중복 투표 체크
			if (votingService.hasVoted(serialNumber, voterId)) {
				redirectAttributes.addFlashAttribute("voteWarning", true);
				redirectAttributes.addFlashAttribute("message", "이미 이 질문에 투표하셨습니다.");
				return "redirect:/voting";
			}

			// 투표 저장
			VotingEntity vote = votingService.saveVote(serialNumber, voterId, votedId);

			if (vote != null && vote.getId() != null) {
				redirectAttributes.addFlashAttribute("voteSuccess", true);
				redirectAttributes.addFlashAttribute("message", "투표가 성공적으로 저장되었습니다.");
			} else {
				redirectAttributes.addFlashAttribute("voteError", true);
				redirectAttributes.addFlashAttribute("errorMessage", "투표 저장에 실패했습니다.");
			}

			return "redirect:/voting";

		} catch (Exception e) {
			System.err.println("투표 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("voteError", true);
			redirectAttributes.addFlashAttribute("errorMessage", "투표 처리 중 오류가 발생했습니다.");
			return "redirect:/voting";
		}
	}
}