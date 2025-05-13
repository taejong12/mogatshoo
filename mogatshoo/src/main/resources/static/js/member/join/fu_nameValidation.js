function fu_nameValidation(){
			
	let nameInput = document.getElementById('memberName');
	
	if(nameInput){
		let nameVal = nameInput.value.trim();
		
		if(nameVal == '' || nameVal.length == 0){
			nameInput.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let nameWarp = nameInput.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let nameWarnDiv = document.createElement('div');
			nameWarnDiv.className = 'warn-div name';
			nameWarnDiv.textContent = '이름을 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(nameWarnDiv);
			
			nameWarp.insertAdjacentElement('afterend', inputWrap);
			return false;
		} else{
			return true;
		}
	} else {
		return true;
	}
}