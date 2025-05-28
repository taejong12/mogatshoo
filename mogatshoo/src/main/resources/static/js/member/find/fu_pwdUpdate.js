function fu_pwdUpdate(){
	
	let pwdUpdateError = document.getElementById('pwdUpdateError');
	pwdUpdateError.textContent = '';
	
	let form = document.pwdUpdateForm;
	
	let pwdCheckVali = fu_pwdCheckVali();
	let pwdVali = fu_pwdVali();
	
	if(pwdVali && pwdCheckVali && pwdInputCheck && pwdCheckEqual){
		form.method= "post";
		form.action = "/member/pwdUpdate";
		form.submit();
	} else{
		pwdUpdateError.textContent = '잘못 입력하셨습니다. 다시 입력해주세요.';
		pwdCheckWarnMsg.style.display = 'inline';
	}
}
