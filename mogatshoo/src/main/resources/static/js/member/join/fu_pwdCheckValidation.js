function fu_pwdCheckValidation(){
	
	let pwdCheckInput = document.getElementById('memberPwdCheck');
	
	if(pwdCheckInput){
		let pwdCheckVal = pwdCheckInput.value.trim();
		
		if(pwdCheckVal == '' || pwdCheckVal.length == 0){
			document.joinForm.memberPwdCheck.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let pwdCheckWarp = document.joinForm.memberPwdCheck.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let pwdCheckWarnDiv = document.createElement('div');
			pwdCheckWarnDiv.className = 'warn-div pwdCheck';
			pwdCheckWarnDiv.textContent = '비밀번호 확인을 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(pwdCheckWarnDiv);
			
			pwdCheckWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			return true;
		}
	}else{
		return true;
	}
}