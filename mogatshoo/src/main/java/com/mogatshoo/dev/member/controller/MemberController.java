package com.mogatshoo.dev.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mogatshoo.dev.common.mail.controller.EmailController;
import com.mogatshoo.dev.hair_loss_test.service.HairLossTestService;
import com.mogatshoo.dev.member.entity.MemberEntity;
import com.mogatshoo.dev.member.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private EmailController emailController;
	@Autowired
	HairLossTestService hairService;
	
	@GetMapping("/login")
	public String loginPage() {
		return "member/login";
	}

	@GetMapping("/join")
	public String joinPage() {
		return "member/join";
	}
	
	@GetMapping("/complete")
	public String memberComplete(HttpSession session, Model model) {
		String postMapping = (String) session.getAttribute("postMapping");
		
		if(postMapping == null) {
			return "redirect:/";
		}
		
		if(postMapping.equals("join")) {
			String memberNickName = (String) session.getAttribute("memberNickName");
			model.addAttribute("memberNickName", memberNickName);
			session.removeAttribute("memberNickName");
		}
		
		model.addAttribute("postMapping", postMapping);
		session.removeAttribute("postMapping");
		session.removeAttribute("memberEntity");
		return "member/complete";
	}

	@PostMapping("/join")
	public String memberSave(@ModelAttribute MemberEntity memberEntity, Model model, HttpSession session) {
		memberService.memberSave(memberEntity);
		session.setAttribute("postMapping", "join");
		session.setAttribute("memberNickName", memberEntity.getMemberNickName());
		return "redirect:/member/complete";
	}
	
	@PostMapping("/idCheck")
	@ResponseBody
	public Map<String, Boolean> memberIdCheck(@RequestBody Map<String, String> request){
		boolean memberIdCheck = memberService.memberIdCheck(request.get("memberId"));
		Map<String, Boolean> map = new HashMap<>();
		map.put("memberIdCheck", memberIdCheck);
		return map;
	}
	
	@PostMapping("/emailCheck")
	@ResponseBody
	public Map<String, Boolean> memberEmailCheck(@RequestBody Map<String, String> request){
		Boolean memberEmailCheck = memberService.memberEmailCheck(request.get("memberEmail"));
		Map<String, Boolean> map = new HashMap<>();
		map.put("memberEmailCheck", memberEmailCheck);
		return map;
	}
	
	@PostMapping("/nickNameCheck")
	@ResponseBody
	public Map<String, Boolean> memberNickNameCheck(@RequestBody Map<String, String> request){
		Boolean memberNickNameCheck = memberService.memberNickNameCheck(request.get("memberNickName"));
		Map<String, Boolean> map = new HashMap<>();
		map.put("memberNickNameCheck", memberNickNameCheck);
		return map;
	}
	
	@PostMapping("/sendEmail")
	@ResponseBody
	public Map<String, Object> memberSendEmail(@RequestBody Map<String, String> request, HttpSession session){
		Map<String, Object> map = new HashMap<>();
		map = emailController.sendAuthEmail(request.get("memberEmail"), session);
		return map;
	}
	
	@PostMapping("/emailAuthCodeConfirm")
	@ResponseBody
	public Map<String, Object> emailAuthCodeConfirm(@RequestBody Map<String, String> request, HttpSession session){
		Map<String, Object> map = new HashMap<>();
		map = emailController.authCodeConfirm(request.get("emailAuthCode"), session);
		return map;
	}
	
	@GetMapping("/mypage")
	public String mypagePage(@RequestParam("memberId") String memberId, Model model) {
		MemberEntity member = memberService.findByMemberId(memberId);
		
		if(member == null) {
			return "redirect:/member/login";
		}
		
		model.addAttribute("member", member);
		return "member/mypage";
	}
	
	@GetMapping("/lost")
	public String lostPage() {
		return "member/lost";
	}
	
	@GetMapping("/findById")
	public String findByIdPage() {
		return "member/findById";
	}
	
	@PostMapping("/findByIdEmailCheck")
	@ResponseBody
	public Map<String, Object> findByIdEmailCheck(@RequestBody Map<String, String> request) {
		MemberEntity member = memberService.findByIdEmailCheck(request.get("memberEmail"));
		Map<String, Object> map = new HashMap<>();
		
		// 이메일이 없으면 true
		boolean memberEmailCheck = true;
		
		// 우리 자체 회원가입 회원만 찾기 가능
		if (member != null && "local".equals(member.getProvider())) {
			memberEmailCheck = false;
		}
		
		map.put("memberEmailCheck", memberEmailCheck);
		return map;
	}
	
	@PostMapping("/findById")
	public String findByIdSendEmail(@ModelAttribute MemberEntity memberEntity, HttpSession session) {
		memberEntity = memberService.findByMemberEmail(memberEntity.getMemberEmail());
		emailController.findByIdSendEmail(memberEntity);
		session.setAttribute("postMapping", "findById");
		return "redirect:/member/complete";
	}
	
	@GetMapping("/findByPwd")
	public String findByPwdPage() {
		return "member/findByPwd";
	}
	
	@PostMapping("/delete")
	public String memberDelete(@RequestParam("memberId") String memberId, HttpSession session) {
		memberService.memberDelete(memberId);
		session.invalidate();
		return "redirect:/";
	}
	
	@GetMapping("/update")
	public String memberUpdatePage(@RequestParam("memberId") String memberId, HttpSession session, Model model) {
		MemberEntity member = memberService.findByMemberId(memberId);
		
		if(member == null) {
			return "redirect:/member/login";
		}
		
		session.setAttribute("member", member);
		model.addAttribute("member", member);
		return "member/update";
	}
	
	@PostMapping("/update")
	public String memberUpdate(@ModelAttribute MemberEntity memberEntity, HttpSession session) {
		
		MemberEntity member = (MemberEntity) session.getAttribute("member");
		
		if(member == null) {
			return "redirect:/member/login";
		}
		
		String memberId = member.getMemberId();
		
		memberEntity.setMemberId(memberId);
		memberService.memberUpdate(memberEntity);
		session.removeAttribute("member");
		return "redirect:/member/mypage?memberId="+memberId;
	}
	
	@PostMapping("/pwdUpdateForm")
	public String pwdUpdateForm(@ModelAttribute MemberEntity memberEntity, HttpSession session) {
		String authSuccess = (String) session.getAttribute("authSuccess");
		
		if(authSuccess == null) {
			return "redirect:/";
		}
		
		String memberId = memberEntity.getMemberId();
		session.setAttribute("memberId", memberId);
		session.removeAttribute("authSuccess");
		return "member/pwdUpdateForm";
	}
	
	@PostMapping("/idAndEmailCheck")
	@ResponseBody
	public Map<String, Object> findByPwdIdAndEmailCheck(@RequestBody Map<String, String> request) {
		
		String memberId = request.get("memberId");
		String memberEmail = request.get("memberEmail");
		
		MemberEntity member = memberService.findByPwdIdAndEmailCheck(memberId, memberEmail);
		
		Map<String, Object> map = new HashMap<>();
		
		// 회원이 없으면 true
		boolean idAndEmailCheck = true;
		
		// 우리 자체 회원가입 회원만 찾기 가능
		if (member != null && "local".equals(member.getProvider())) {
		    if (memberId != null && memberEmail != null) {
		    	idAndEmailCheck = false;
		    }
		}
		
		map.put("idAndEmailCheck", idAndEmailCheck);
		return map;
	}
	
	@PostMapping("/pwdUpdate")
	public String pwdUpdate(@ModelAttribute MemberEntity memberEntity, HttpSession session) {
		String memberId = (String) session.getAttribute("memberId");
		memberEntity.setMemberId(memberId);
		memberService.pwdUpdate(memberEntity);
		session.setAttribute("postMapping", "findByPwd");
		session.removeAttribute("memberId");
		return "redirect:/member/complete";
	}
}
