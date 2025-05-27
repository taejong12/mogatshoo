// 회원가입 버튼
function fu_memberJoin(){

	let joingForm = document.joinForm;
	
	let emailAuthValidation = fu_emailAuthValidation();

	if(!emailAuthCheck && joingForm.emailAuth){
		joingForm.emailAuth.focus();
	}
	
	let emailValidation = fu_emailValidation();
	let addrValidation = fu_addrValidation();
	let genderValidation = fu_genderValidation();
	let birthValidation = fu_birthValidation();
	let telValidation = fu_telValidation();
	
	if(!nickNameCheck){
		joingForm.memberNickName.focus();
	}
	
	let nickNameValidation = fu_nickNameValidation();
	let nameValidation = fu_nameValidation();
	let pwdCheckValidation = fu_pwdCheckValidation();
	
	if(!pwdCheckEqual){
		joingForm.memberPwdCheck.focus();
	}
	
	if(!pwdInputCheck){
		joingForm.memberPwd.focus();
	}
	
	let pwdValidation = fu_pwdValidation();
	
	if(!idCheck){
		joingForm.memberId.focus();
	}
	
	let idValidation = fu_idValidation();
	
	console.log(idValidation);
	console.log(pwdValidation);
	console.log(pwdCheckValidation);
	console.log(nameValidation);
	console.log(telValidation);
	console.log(birthValidation);
	console.log(emailValidation);
	console.log(pwdInputCheck);
	console.log(pwdCheckEqual);
	console.log(emailAuthCheck);
	console.log(emailAuthValidation);
	console.log(addrValidation);
	console.log(genderValidation);
	console.log(nickNameValidation);
	console.log(idCheck);
	console.log(nickNameCheck);
	console.log(emailCheck);
	
	
	if(idValidation && pwdValidation && pwdCheckValidation 
		&& nameValidation && telValidation && birthValidation 
		&& emailValidation && pwdInputCheck 
		&& pwdCheckEqual && emailAuthCheck && emailAuthValidation 
		&& addrValidation && genderValidation && nickNameValidation
		&& idCheck && nickNameCheck && emailCheck){
		joingForm.method= "post";
		joingForm.action = "/member/join";
		joingForm.submit();
	}
}