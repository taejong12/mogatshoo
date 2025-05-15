function fu_infoCheckValidation(){
	
	let infoInput = document.getElementById('memberInfoCheck');
	
	if(infoInput){
		let infoCheck = infoInput.checked;
		
		if(!infoCheck){
			infoInput.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "13px";
	        
			let infoCheckWarp = infoInput.closest('.info-input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let infoCheckWarnDiv = document.createElement('div');
			infoCheckWarnDiv.className = 'warn-div infoCheck';
			infoCheckWarnDiv.textContent = '개인정보 수집 동의해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(infoCheckWarnDiv);
			
			infoInput.value = 'N';
			
			infoCheckWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			infoInput.value = 'Y';
			return true;
		}	
	} else {
		return true;
	}
}