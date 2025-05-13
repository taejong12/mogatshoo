function fu_emailAuthValidation(){
	
	let emailAuthInput = document.getElementById('emailAuth');
	
	if(emailAuthInput){
		
		let emailAuthVal = emailAuthInput.value.trim();
			
		if(emailAuthVal == '' || emailAuthVal.length == 0){
			emailAuthInput.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let emailAuthWarp = emailAuthInput.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let emailAuthWarnDiv = document.createElement('div');
			emailAuthWarnDiv.className = 'warn-div emailAuth';
			emailAuthWarnDiv.textContent = '인증번호를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(emailAuthWarnDiv);
			
			emailAuthWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			return true;
		}	
	}
	return false;
}