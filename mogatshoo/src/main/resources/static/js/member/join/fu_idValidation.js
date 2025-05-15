function fu_idValidation(){
	
	let idInput = document.getElementById('memberId');
	
	if(idInput){
		let idVal = idInput.value.trim();
		
		if(idVal == '' || idVal.length == 0){
			idInput.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let idWarp = idInput.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let idWarnDiv = document.createElement('div');
			idWarnDiv.className = 'warn-div id';
			idWarnDiv.textContent = '아이디를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(idWarnDiv);
			
			idWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			return true;
		}
	} else {
		idCheck = true;
		return true;
	}
}