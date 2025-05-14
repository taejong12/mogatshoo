function fu_emailValidation(){
	let emailInput = document.getElementById('memberEmail');
	
	if(emailInput){
		let emailVal = emailInput.value.trim();
		
		let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
	    
		let emailWarp = emailInput.closest('.input-wrap');
		
		let inputWrap = document.createElement('div');
		inputWrap.className = 'input-wrap d-flex align-items-center';
		
		let emailWarnDiv = document.createElement('div');
		emailWarnDiv.className = 'warn-div email';
		
		if(emailVal == '' || emailVal.length == 0){
			emailInput.focus();
			
			emailWarnDiv.textContent = '이메일을 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(emailWarnDiv);
			
			emailWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			let emailCheckInput = document.getElementById('emailCheckMessage');
			let emailAuthInput = document.getElementById('emailAuth');
			
			if(emailCheckInput.classList.contains('valid') && !emailAuthInput){
				emailInput.focus();
				
				emailCheckInput.textContent = '';
				emailWarnDiv.textContent = '이메일을 인증해주세요.';
				
				inputWrap.appendChild(spaceDiv);
				inputWrap.appendChild(emailWarnDiv);
				
				emailWarp.insertAdjacentElement('afterend', inputWrap);
				return false;
			}
			return true;
		}	
	} else {
		emailAuthCheck = true;
		return true;
	}
}