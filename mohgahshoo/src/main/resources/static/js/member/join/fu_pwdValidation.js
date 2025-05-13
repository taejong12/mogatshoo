function fu_pwdValidation(){
	let pwdVal = document.joinForm.memberPwd.value.trim();
	
	if(pwdVal == '' || pwdVal.length == 0){
		document.joinForm.memberPwd.focus();
		
        let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
        
		let pwdWarp = document.joinForm.memberPwd.closest('.input-wrap');
		
		let inputWrap = document.createElement('div');
		inputWrap.className = 'input-wrap d-flex align-items-center';
		
		let pwdWarnDiv = document.createElement('div');
		pwdWarnDiv.className = 'warn-div pwd';
		pwdWarnDiv.textContent = '비밀번호를 입력해주세요.';
		
		inputWrap.appendChild(spaceDiv);
		inputWrap.appendChild(pwdWarnDiv);
		
		pwdWarp.insertAdjacentElement('afterend', inputWrap);
		return false;
	} else{
		return true;
	}
}