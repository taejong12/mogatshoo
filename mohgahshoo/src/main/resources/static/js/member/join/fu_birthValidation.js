function fu_birthValidation(){
			
	let birthInput = document.getElementById('memberBirth');
	let birthVal = birthInput.value.trim();
	
	if(birthVal == '' || birthVal.length == 0){
		birthInput.focus();
		
        let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
        
		let birthWarp = birthInput.closest('.input-wrap');
		
		let inputWrap = document.createElement('div');
		inputWrap.className = 'input-wrap d-flex align-items-center';
		
		let birthWarnDiv = document.createElement('div');
		birthWarnDiv.className = 'warn-div birth';
		birthWarnDiv.textContent = '생년월일을 입력해주세요.';
		
		inputWrap.appendChild(spaceDiv);
		inputWrap.appendChild(birthWarnDiv);
		
		birthWarp.insertAdjacentElement('afterend', inputWrap);
		return false;
	} else{
		return true;
	}
}