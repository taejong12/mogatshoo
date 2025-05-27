function fu_pwdCheckValidation(){
	
	let pwdCheckInput = document.getElementById('memberPwdCheck');
	let pwdCheckMsg = document.getElementById('pwdCheckMsg');
	
	if(pwdCheckInput){
		pwdCheckMsg.textContent = '';
		
		let pwdCheckVal = pwdCheckInput.value.trim();
		
		if(pwdCheckVal == '' || pwdCheckVal.length == 0){
			pwdCheckInput.focus();
	        pwdCheckMsg.textContent = '비밀번호 확인을 입력해주세요.';
			pwdCheckMsg.style.color = 'rgb(255, 107, 107)';
			return false;
		} else{
			return true;
		}
	}else{
		return true;
	}
}