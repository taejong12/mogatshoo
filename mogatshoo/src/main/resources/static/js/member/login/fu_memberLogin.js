function fu_memberLogin(){
	let loginForm = document.loginForm;
	let idInput = loginForm.memberId;
	let pwdInput = loginForm.memberPwd;
	let idWarnMsg = document.getElementById('idWarnMsg');
	let pwdWarnMsg = document.getElementById('pwdWarnMsg');
	let loginError = document.getElementById('loginError');
	
	let idCheck = false;
	let pwdCheck = false;
	
	idWarnMsg.textContent = '';
	pwdWarnMsg.textContent = '';

	if(pwdInput.value == "" || pwdInput.value.length == 0){
		pwdInput.focus();
		pwdWarnMsg.textContent = '비밀번호를 입력해주세요.';
		pwdWarnMsg.style.display = 'inline';
		pwdCheck = false;
	}else{
		pwdCheck = true;
	}
	
	if(idInput.value == "" || idInput.value.length == 0){
		idInput.focus();
		idWarnMsg.textContent = '아이디를 입력해주세요.';
		idWarnMsg.style.display = 'inline';
		idCheck = false;
	}else{
		idCheck = true;
	}
	
	if(idCheck && pwdCheck){
		loginForm.method= "post";
		loginForm.action= "/member/login";
		loginForm.submit();
	}
}