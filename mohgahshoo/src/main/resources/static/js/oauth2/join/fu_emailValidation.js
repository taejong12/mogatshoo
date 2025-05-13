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
			return true;
		}	
	} else {
		return true;
	}
}