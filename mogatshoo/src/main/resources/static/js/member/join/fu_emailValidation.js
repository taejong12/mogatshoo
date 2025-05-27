function fu_emailValidation(){
	
	let emailInput = document.getElementById('memberEmail');
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	
	
	if(emailInput){
		emailWarnMsg.textContent = '';
		
		let emailVal = emailInput.value.trim();
		
		if(emailVal == '' || emailVal.length == 0){
			emailInput.focus();
			emailWarnMsg.textContent = '이메일을 입력해주세요.';
			emailWarnMsg.style.color = 'rgb(255, 107, 107)';
			return false;
		} else{
			// 이메일 인증성공이면 통과
			if(!emailAuthCheck){
				emailInput.focus();
				emailWarnMsg.textContent = '이메일을 인증해주세요.';
				emailWarnMsg.style.color = 'rgb(255, 107, 107)';
				return false;
			}
			return true;
		}	
	} else {
		emailAuthCheck = true;
		return true;
	}
}