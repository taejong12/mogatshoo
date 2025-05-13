// 회원가입 버튼
function fu_memberJoin(){

	let form = document.joinForm;
	
	// 경고 문구 초기화
	fu_warnCheck();
	
	// 유효성 검사
	let infoCheckValidation = fu_infoCheckValidation();
	let emailAuthValidation = fu_emailAuthValidation();

	if(!emailAuthCheck && form.emailAuth){
		form.emailAuth.focus();
	}
	
	let emailValidation = fu_emailValidation();
	
	
	let addrValidation = fu_addrValidation();
	let genderValidation = fu_genderValidation();
	
	let birthValidation = fu_birthValidation();
	let telValidation = fu_telValidation();
	let nickNameValidation = fu_nickNameValidation();
	let nameValidation = fu_nameValidation();
	let pwdCheckValidation = fu_pwdCheckValidation();
	
	if(!pwdCheckEqual){
		form.memberPwdCheck.focus();
	}
	
	if(!pwdInputCheck){
		form.memberPwd.focus();
	}
	
	let pwdValidation = fu_pwdValidation();
	let idValidation = fu_idValidation();
	
	if(idValidation && pwdValidation && pwdCheckValidation 
		&& nameValidation && telValidation && birthValidation 
		&& emailValidation && infoCheckValidation && pwdInputCheck 
		&& pwdCheckEqual && emailAuthCheck && emailAuthValidation 
		&& addrValidation && genderValidation && nickNameValidation){
		form.method= "post";
		form.action = "/member/join";
		form.submit();
	}
}