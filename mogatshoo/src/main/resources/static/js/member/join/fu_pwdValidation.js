function fu_pwdValidation(){
	
	let pwdInput = document.getElementById('memberPwd');
	let pwdWarnMsg = document.getElementById('pwdWarnMsg');
	
	if(pwdInput){
		pwdWarnMsg.textContent = '';
		
		let pwdVal = pwdInput.value.trim();
		
		if(pwdVal == '' || pwdVal.length == 0){
			pwdInput.focus();
	        pwdWarnMsg.textContent = '비밀번호를 입력해주세요.';
			pwdWarnMsg.style.color = 'rgb(255, 107, 107)';
			pwdWarnMsg.style.display = 'inline';
			return false;
		} else{
			return true;
		}
	} else {
		pwdInputCheck = true;
		pwdCheckEqual = true;
		return true;
	}
}