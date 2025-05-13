function fu_telValidation(){
	
	let telInput = document.getElementById('memberTel');
	
	if(telInput){
		let telVal = telInput.value.trim();
		
		let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
	    
		let telWarp = telInput.closest('.input-wrap');
		
		let inputWrap = document.createElement('div');
		inputWrap.className = 'input-wrap d-flex align-items-center';
		
		let telWarnDiv = document.createElement('div');
		telWarnDiv.className = 'warn-div tel';
		
		if(telVal == '' || telVal.length == 0){
			telInput.focus();
			
			telWarnDiv.textContent = '전화번호를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(telWarnDiv);
			
			telWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			let telInput = document.getElementById('memberTel');
			let telPattern = /^01[016789]\d{7,8}$/;
	
		    if (!telPattern.test(telInput.value.trim())) {
		        telInput.focus();
				telWarnDiv.textContent = '전화번호를 올바르게 입력하세요.';
				inputWrap.appendChild(spaceDiv);
				inputWrap.appendChild(telWarnDiv);
				
				telWarp.insertAdjacentElement('afterend', inputWrap);
		        return false;
		    }
			return true;
		}
	} else{
		return true;
	}
}