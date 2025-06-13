package com.mogatshoo.dev.admin.voting_status.controller;

import com.mogatshoo.dev.admin.amdinEmail.service.AdminEmailService;
import com.mogatshoo.dev.admin.voting_status.entity.StatusEntity;
import com.mogatshoo.dev.admin.voting_status.service.StatusService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/voting-status")
public class StatusController {

	@Autowired
	private StatusService statusService;

	@Autowired
	private AdminEmailService adminEmailService;

	@Autowired
	private MemberService memberService;

	// 기존 투표 관리 현황 페이지 (페이지네이션 추가) - 수정된 부분
	@GetMapping("")
	public String votingStatusPage(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size, Model model) {
		try {
			// 페이지네이션 설정 (최신순 정렬)
			Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

			// 페이징된 투표 통계 조회
			Page<StatusEntity> votingStatisticsPage = statusService.getAllVotingStatistics(pageable);

			// 데이터가 없는 경우 처리
			if (votingStatisticsPage.isEmpty()) {
				model.addAttribute("noData", true);
				model.addAttribute("message", "현재 등록된 질문이 없습니다.");
				model.addAttribute("votingStatistics", new ArrayList<>());
				model.addAttribute("totalQuestions", 0);
				model.addAttribute("pendingQuestions", 0);
				model.addAttribute("activeQuestions", 0);
				model.addAttribute("endedQuestions", 0);
				model.addAttribute("eligibleQuestions", 0);
				model.addAttribute("totalMembers", 0);

				// 페이지네이션 정보
				model.addAttribute("currentPage", 0);
				model.addAttribute("totalPages", 0);
				model.addAttribute("totalElements", 0);
				model.addAttribute("hasNext", false);
				model.addAttribute("hasPrevious", false);

				return "admin/voting/status";
			}

			List<StatusEntity> votingStatistics = votingStatisticsPage.getContent();

			// 각 질문별 이메일 전송 여부 확인 및 최다득표자 정보 콘솔 출력
			Map<String, Boolean> emailSentStatus = new HashMap<>();
			for (StatusEntity status : votingStatistics) {
				if (status.getTopVotedId() != null) {
					boolean emailSent = adminEmailService.isDuplicateEmail(status.getSerialNumber(),
							status.getTopVotedId());
					emailSentStatus.put(status.getSerialNumber(), emailSent);

					// 최다득표자 상세 정보 콘솔 출력
					logTopVotedMemberInfo(status);
				} else {
					emailSentStatus.put(status.getSerialNumber(), false);
				}
			}

			// 기본 통계 정보
			model.addAttribute("votingStatistics", votingStatistics);
			model.addAttribute("emailSentStatus", emailSentStatus);

			// 전체 통계 (페이징과 별도로 계산)
			int totalQuestions = (int) votingStatisticsPage.getTotalElements();
			model.addAttribute("totalQuestions", totalQuestions);

			// 상태별 통계 계산 (동적 상태 기준)
			long pendingQuestions = votingStatistics.stream().filter(stat -> "보류".equals(stat.getCurrentVotingStatus()))
					.count();
			long activeQuestions = votingStatistics.stream().filter(stat -> "진행중".equals(stat.getCurrentVotingStatus()))
					.count();
			long endedQuestions = votingStatistics.stream().filter(stat -> "종료".equals(stat.getCurrentVotingStatus()))
					.count();

			model.addAttribute("pendingQuestions", pendingQuestions);
			model.addAttribute("activeQuestions", activeQuestions);
			model.addAttribute("endedQuestions", endedQuestions);

			// **업데이트된 이메일 전송 가능한 질문 수 계산**
			// (종료되고 참여율 50% 이상이며 최다득표율 40% 이상)
			long eligibleQuestions = votingStatistics.stream()
					.filter(stat -> "종료".equals(stat.getCurrentVotingStatus())
							&& stat.getParticipationRate() != null && stat.getParticipationRate() >= 50.0
							&& stat.getTopVotedRate() != null && stat.getTopVotedRate() >= 40.0)
					.count();
			model.addAttribute("eligibleQuestions", eligibleQuestions);

			// 전체 회원 수
			if (!votingStatistics.isEmpty()) {
				model.addAttribute("totalMembers", votingStatistics.get(0).getTotalMembers());
			} else {
				model.addAttribute("totalMembers", 0);
			}

			// 페이지네이션 정보
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", votingStatisticsPage.getTotalPages());
			model.addAttribute("totalElements", votingStatisticsPage.getTotalElements());
			model.addAttribute("hasNext", votingStatisticsPage.hasNext());
			model.addAttribute("hasPrevious", votingStatisticsPage.hasPrevious());
			model.addAttribute("pageSize", size);

			// 페이지 번호 리스트 생성 (현재 페이지 기준 ±2)
			List<Integer> pageNumbers = new ArrayList<>();
			int totalPages = votingStatisticsPage.getTotalPages();
			int startPage = Math.max(0, page - 2);
			int endPage = Math.min(totalPages - 1, page + 2);

			for (int i = startPage; i <= endPage; i++) {
				pageNumbers.add(i);
			}
			model.addAttribute("pageNumbers", pageNumbers);

			return "admin/voting/status";

		} catch (Exception e) {
			System.err.println("투표 현황 페이지 로딩 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("errorMessage", "투표 현황을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
			model.addAttribute("noData", true);

			// 오류 시에도 페이지네이션 기본값 설정
			model.addAttribute("currentPage", 0);
			model.addAttribute("totalPages", 0);
			model.addAttribute("totalElements", 0);
			model.addAttribute("hasNext", false);
			model.addAttribute("hasPrevious", false);

			return "admin/voting/status";
		}
	}

	// 특정 질문의 상세 통계 조회
	@GetMapping("/detail/{serialNumber}")
	@ResponseBody
	public StatusEntity getQuestionDetail(@PathVariable String serialNumber) {
		try {
			StatusEntity questionDetail = statusService.getQuestionStatistics(serialNumber);
			
			// 상세 조회 시에도 최다득표자 정보 콘솔 출력
			if (questionDetail != null) {
				logTopVotedMemberInfo(questionDetail);
			}
			
			return questionDetail;
		} catch (Exception e) {
			System.err.println("질문 상세 통계 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	// **업데이트된 이메일 전송 페이지로 이동 - 새로운 조건 적용**
	@GetMapping("/email/{serialNumber}")
	public String emailPage(@PathVariable String serialNumber, Model model, RedirectAttributes redirectAttributes) {
		try {
			// 해당 질문의 통계 정보 조회
			StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);

			if (questionStats == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "질문 정보를 찾을 수 없습니다.");
				return "redirect:/admin/voting-status";
			}

			// 이메일 페이지 이동 시 최다득표자 정보 콘솔 출력
			logTopVotedMemberInfo(questionStats);

			// 종료된 질문인지 확인 (동적 상태 확인)
			if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
				redirectAttributes.addFlashAttribute("errorMessage", "투표가 종료된 질문만 이메일을 전송할 수 있습니다.");
				return "redirect:/admin/voting-status";
			}

			// **새로운 조건 1: 참여율 50% 이상인지 확인**
			if (questionStats.getParticipationRate() < 50.0) {
				redirectAttributes.addFlashAttribute("errorMessage",
						String.format("참여율이 50%% 미만입니다. (현재: %.1f%%, 참여자: %d명)", 
						questionStats.getParticipationRate(),
						questionStats.getUniqueVoters()));
				return "redirect:/admin/voting-status";
			}

			// **새로운 조건 2: 최다득표자 득표율 40% 이상인지 확인**
			if (questionStats.getTopVotedRate() < 40.0) {
				redirectAttributes.addFlashAttribute("errorMessage",
						String.format("최다득표자의 득표율이 40%% 미만입니다. (현재: %.1f%%, %s님 %d표)", 
						questionStats.getTopVotedRate(),
						questionStats.getTopVotedName() != null ? questionStats.getTopVotedName() : "없음",
						questionStats.getTopVoteCount() != null ? questionStats.getTopVoteCount() : 0));
				return "redirect:/admin/voting-status";
			}

			// 이미 전송했는지 확인
			if (adminEmailService.isDuplicateEmail(questionStats.getSerialNumber(), questionStats.getTopVotedId())) {
				redirectAttributes.addFlashAttribute("warningMessage", "이미 해당 당첨자에게 이메일을 전송했습니다.");
				return "redirect:/admin/email/history/" + serialNumber;
			}

			model.addAttribute("questionStats", questionStats);
			model.addAttribute("serialNumber", serialNumber);

			return "redirect:/admin/email/send/" + serialNumber;
		} catch (Exception e) {
			System.err.println("이메일 페이지 이동 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "이메일 페이지로 이동하는 중 오류가 발생했습니다.");
			return "redirect:/admin/voting-status";
		}
	}

	// 투표 통계 새로고침
	@PostMapping("/refresh")
	public String refreshStatistics(@RequestParam(value = "page", defaultValue = "0") int page,
			RedirectAttributes redirectAttributes) {
		try {
			// 통계 데이터 새로고침 (캐시 클리어 등)
			statusService.refreshStatistics();
			redirectAttributes.addFlashAttribute("successMessage", "투표 통계가 새로고침되었습니다.");
		} catch (Exception e) {
			System.err.println("통계 새로고침 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "통계 새로고침 중 오류가 발생했습니다.");
		}
		return "redirect:/admin/voting-status?page=" + page;
	}

	// **업데이트된 빠른 이메일 전송 API - 새로운 조건 적용**
	@PostMapping("/quick-email/{serialNumber}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> quickSendEmail(@PathVariable String serialNumber) {
		Map<String, Object> response = new HashMap<>();

		try {
			// 투표 통계 조회
			StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);

			if (questionStats == null) {
				response.put("success", false);
				response.put("message", "질문 정보를 찾을 수 없습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// 빠른 이메일 전송 시에도 최다득표자 정보 콘솔 출력
			logTopVotedMemberInfo(questionStats);

			// 종료된 질문인지 확인 (동적 상태 확인)
			if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
				response.put("success", false);
				response.put("message", "투표가 종료된 질문만 이메일을 전송할 수 있습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// **새로운 조건 1: 참여율 50% 이상인지 확인**
			if (questionStats.getParticipationRate() < 50.0) {
				response.put("success", false);
				response.put("message",
						String.format("참여율이 50%% 미만입니다. (현재: %.1f%%)", questionStats.getParticipationRate()));
				return ResponseEntity.badRequest().body(response);
			}

			// **새로운 조건 2: 최다득표자 득표율 40% 이상인지 확인**
			if (questionStats.getTopVotedRate() < 40.0) {
				response.put("success", false);
				response.put("message",
						String.format("최다득표자의 득표율이 40%% 미만입니다. (현재: %.1f%%)", questionStats.getTopVotedRate()));
				return ResponseEntity.badRequest().body(response);
			}

			// 중복 전송 체크
			if (adminEmailService.isDuplicateEmail(serialNumber, questionStats.getTopVotedId())) {
				response.put("success", false);
				response.put("message", "이미 해당 당첨자에게 이메일을 전송했습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			response.put("success", true);
			response.put("message", "이메일 전송 페이지로 이동합니다.");
			response.put("redirectUrl", "/admin/email/send/" + serialNumber);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("빠른 이메일 전송 체크 중 오류: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "시스템 오류가 발생했습니다.");
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * **업데이트된 통계 계산 메서드 - 새로운 이메일 발송 조건 반영**
	 */
	private void calculateAndSetStatistics(List<StatusEntity> votingStatistics, Page<StatusEntity> votingStatisticsPage,
			Model model) {
		// 전체 통계
		int totalQuestions = (int) votingStatisticsPage.getTotalElements();
		model.addAttribute("totalQuestions", totalQuestions);

		// 상태별 통계 계산
		long pendingQuestions = votingStatistics.stream().filter(stat -> "보류".equals(stat.getCurrentVotingStatus())).count();
		long activeQuestions = votingStatistics.stream().filter(stat -> "진행중".equals(stat.getCurrentVotingStatus())).count();
		long endedQuestions = votingStatistics.stream().filter(stat -> "종료".equals(stat.getCurrentVotingStatus())).count();

		// 완료 상태별 통계
		long completedQuestions = votingStatistics.stream().filter(stat -> Boolean.TRUE.equals(stat.getIsCompleted())).count();

		model.addAttribute("pendingQuestions", pendingQuestions);
		model.addAttribute("activeQuestions", activeQuestions);
		model.addAttribute("endedQuestions", endedQuestions);
		model.addAttribute("completedQuestions", completedQuestions);

		// **새로운 이메일 전송 가능한 질문 수 - 업데이트된 조건**
		// (종료되고 참여율 50% 이상이며 최다득표율 40% 이상)
		long eligibleQuestions = votingStatistics.stream()
				.filter(stat -> "종료".equals(stat.getCurrentVotingStatus())
						&& stat.getParticipationRate() != null && stat.getParticipationRate() >= 50.0
						&& stat.getTopVotedRate() != null && stat.getTopVotedRate() >= 40.0)
				.count();
		model.addAttribute("eligibleQuestions", eligibleQuestions);

		// 전체 회원 수
		if (!votingStatistics.isEmpty()) {
			model.addAttribute("totalMembers", votingStatistics.get(0).getTotalMembers());
		} else {
			model.addAttribute("totalMembers", 0);
		}
	}

	/**
	 * **신규 추가: 이메일 발송 조건 상세 확인 API**
	 */
	@GetMapping("/email-eligibility/{serialNumber}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> checkEmailEligibility(@PathVariable String serialNumber) {
		Map<String, Object> response = new HashMap<>();

		try {
			StatusEntity questionStats = statusService.getQuestionStatistics(serialNumber);

			if (questionStats == null) {
				response.put("success", false);
				response.put("message", "질문 정보를 찾을 수 없습니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// 이메일 발송 조건 확인 시에도 최다득표자 정보 콘솔 출력
			logTopVotedMemberInfo(questionStats);

			// 조건별 상세 체크
			Map<String, Object> eligibilityDetails = new HashMap<>();
			eligibilityDetails.put("serialNumber", serialNumber);
			eligibilityDetails.put("questionContent", questionStats.getQuestionContent());
			eligibilityDetails.put("votingStatus", questionStats.getCurrentVotingStatus());
			eligibilityDetails.put("isVotingEnded", "종료".equals(questionStats.getCurrentVotingStatus()));
			
			// 참여율 체크
			eligibilityDetails.put("participationRate", questionStats.getParticipationRate());
			eligibilityDetails.put("hasEnoughParticipation", questionStats.getParticipationRate() >= 50.0);
			eligibilityDetails.put("participationThreshold", 50.0);
			
			// 득표율 체크
			eligibilityDetails.put("topVotedRate", questionStats.getTopVotedRate());
			eligibilityDetails.put("hasEnoughTopVotedRate", questionStats.getTopVotedRate() >= 40.0);
			eligibilityDetails.put("topVotedThreshold", 40.0);
			
			// 최다득표자 정보
			eligibilityDetails.put("topVotedName", questionStats.getTopVotedName());
			eligibilityDetails.put("topVoteCount", questionStats.getTopVoteCount());
			eligibilityDetails.put("totalVotes", questionStats.getTotalVotes());
			
			// 전체 자격 여부
			boolean isEligible = questionStats.isEmailEligible();
			eligibilityDetails.put("isOverallEligible", isEligible);
			
			// 상세 메시지
			if (isEligible) {
				eligibilityDetails.put("message", "이메일 전송 조건을 모두 만족합니다.");
			} else {
				List<String> failureReasons = new ArrayList<>();
				if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
					failureReasons.add("투표가 아직 종료되지 않음");
				}
				if (questionStats.getParticipationRate() < 50.0) {
					failureReasons.add(String.format("참여율 부족 (%.1f%% < 50%%)", questionStats.getParticipationRate()));
				}
				if (questionStats.getTopVotedRate() < 40.0) {
					failureReasons.add(String.format("득표율 부족 (%.1f%% < 40%%)", questionStats.getTopVotedRate()));
				}
				eligibilityDetails.put("message", "조건 미충족: " + String.join(", ", failureReasons));
			}

			response.put("success", true);
			response.put("data", eligibilityDetails);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("이메일 발송 조건 확인 중 오류: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "시스템 오류가 발생했습니다.");
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * **신규 추가: 최다득표자 정보 콘솔 출력 메서드**
	 */
	private void logTopVotedMemberInfo(StatusEntity questionStats) {
		try {
			if (questionStats == null || questionStats.getTopVotedId() == null) {
				System.out.println("🏆 ===== 최다득표자 정보 없음 =====");
				System.out.println("질문 번호: " + (questionStats != null ? questionStats.getSerialNumber() : "알 수 없음"));
				System.out.println("상태: 최다득표자 없음");
				System.out.println("=============================");
				return;
			}

			// 최다득표자의 회원 정보 조회
			MemberEntity topVotedMember = memberService.findByMemberId(questionStats.getTopVotedId());

			System.out.println("🏆 ===== 최다득표자 정보 =====");
			System.out.println("질문 번호: " + questionStats.getSerialNumber());
			System.out.println("질문 내용: " + questionStats.getQuestionContent());
			System.out.println("투표 상태: " + questionStats.getCurrentVotingStatus());
			System.out.println("총 투표 수: " + questionStats.getTotalVotes());
			System.out.println("참여자 수: " + questionStats.getUniqueVoters());
			System.out.println("참여율: " + String.format("%.1f%%", questionStats.getParticipationRate()));
			System.out.println("--- 최다득표자 ---");
			System.out.println("회원 ID: " + questionStats.getTopVotedId());
			System.out.println("득표 수: " + questionStats.getTopVoteCount());
			System.out.println("득표율: " + String.format("%.1f%%", questionStats.getTopVotedRate()));
			
			if (topVotedMember != null) {
				System.out.println("✅ 최다득표자 상세 정보:");
				System.out.println("  실명: " + topVotedMember.getMemberName());
				System.out.println("  닉네임: " + topVotedMember.getMemberNickName());
				System.out.println("  이메일: " + topVotedMember.getMemberEmail());
				System.out.println("  성별: " + topVotedMember.getMemberGender());
				System.out.println("  전화번호: " + topVotedMember.getMemberTel());
				System.out.println("  가입일: " + topVotedMember.getMemberCreate());
				System.out.println("  제공업체: " + topVotedMember.getProvider());
			} else {
				System.out.println("❌ 회원 정보: 조회 실패 (탈퇴 또는 존재하지 않는 회원)");
			}
			
			// 이메일 전송 가능 여부 상세 분석
			boolean isEmailEligible = "종료".equals(questionStats.getCurrentVotingStatus()) 
					&& questionStats.getParticipationRate() >= 50.0 
					&& questionStats.getTopVotedRate() >= 40.0;
			
			System.out.println("--- 이메일 전송 조건 분석 ---");
			System.out.println("투표 종료 여부: " + ("종료".equals(questionStats.getCurrentVotingStatus()) ? "✅ YES" : "❌ NO"));
			System.out.println("참여율 50% 이상: " + (questionStats.getParticipationRate() >= 50.0 ? 
					"✅ YES (" + String.format("%.1f%%", questionStats.getParticipationRate()) + ")" : 
					"❌ NO (" + String.format("%.1f%%", questionStats.getParticipationRate()) + ")"));
			System.out.println("득표율 40% 이상: " + (questionStats.getTopVotedRate() >= 40.0 ? 
					"✅ YES (" + String.format("%.1f%%", questionStats.getTopVotedRate()) + ")" : 
					"❌ NO (" + String.format("%.1f%%", questionStats.getTopVotedRate()) + ")"));
			
			if (isEmailEligible && topVotedMember != null) {
				System.out.println("🎉 이메일 전송 가능: YES");
				System.out.println("🏅 우승자 확정: " + topVotedMember.getMemberName() + " (" + topVotedMember.getMemberEmail() + ")");
			} else {
				System.out.println("⚠️ 이메일 전송 가능: NO");
				if (!isEmailEligible) {
					List<String> reasons = new ArrayList<>();
					if (!"종료".equals(questionStats.getCurrentVotingStatus())) {
						reasons.add("투표 미종료");
					}
					if (questionStats.getParticipationRate() < 50.0) {
						reasons.add("참여율 부족");
					}
					if (questionStats.getTopVotedRate() < 40.0) {
						reasons.add("득표율 부족");
					}
					System.out.println("전송 불가 사유: " + String.join(", ", reasons));
				}
			}
			
			System.out.println("=============================");

		} catch (Exception e) {
			System.err.println("최다득표자 정보 출력 중 오류: " + e.getMessage());
			System.out.println("🏆 ===== 최다득표자 정보 =====");
			System.out.println("질문 번호: " + (questionStats != null ? questionStats.getSerialNumber() : "알 수 없음"));
			System.out.println("오류 발생: " + e.getMessage());
			System.out.println("=============================");
		}
	}
}