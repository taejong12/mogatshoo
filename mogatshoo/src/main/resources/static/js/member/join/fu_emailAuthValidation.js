function fu_emailAuthValidation(){
	
	let emailAuthInput = document.getElementById('emailAuth');
	let emailInput = document.getElementById('memberEmail');
	let authWarnMsg = document.getElementById('authWarnMsg');
	
	
	if(emailAuthInput && emailInput){
		authWarnMsg.textContent = '';
		
		let emailAuthVal = emailAuthInput.value.trim();
			
		if(emailAuthVal == '' || emailAuthVal.length == 0){
			emailAuthInput.focus();
			authWarnMsg.textContent = '인증번호를 입력해주세요.';
			authWarnMsg.style.color = 'rgb(255, 107, 107)';
			return false;
		} else{
			return true;
		}	
	} else{
		return true;
	}
}