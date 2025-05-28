function fu_pwdCheckValidation(){
	
	let pwdCheckInput = document.getElementById('memberPwdCheck');
	let pwdCheckWarnMsg = document.getElementById('pwdCheckWarnMsg');
	
	if(pwdCheckInput){
		pwdCheckWarnMsg.textContent = '';
		
		let pwdCheckVal = pwdCheckInput.value.trim();
		
		if(pwdCheckVal == '' || pwdCheckVal.length == 0){
			pwdCheckInput.focus();
	        pwdCheckWarnMsg.textContent = '비밀번호 확인을 입력해주세요.';
			pwdCheckWarnMsg.style.color = 'rgb(255, 107, 107)';
			pwdCheckWarnMsg.style.display = 'inline';
			return false;
		} else{
			return true;
		}
	}else{
		return true;
	}
}