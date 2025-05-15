function fu_pwdVali(){
	
	let pwdInput = document.getElementById('memberPwd');
	let pwdWarnMsg = document.getElementById('pwdWarnMsg');
	
	pwdWarnMsg.textContent = '';
	
	if(pwdInput){
		let pwdVal = pwdInput.value.trim();
		
		if(pwdVal == '' || pwdVal.length == 0){
			pwdInput.focus();
			pwdWarnMsg.textContent = '비밀번호를 입력해주세요.';
			return false;
		} else{
			return true;
		}
	} else {
		return true;
	}
}