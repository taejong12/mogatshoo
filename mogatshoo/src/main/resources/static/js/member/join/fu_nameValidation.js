function fu_nameValidation(){
			
	let nameInput = document.getElementById('memberName');
	let nameWarnMsg = document.getElementById('nameWarnMsg');
	
	if(nameInput){
		nameWarnMsg.textContent = '';
		
		let nameVal = nameInput.value.trim();
		
		if(nameVal == '' || nameVal.length == 0){
			nameInput.focus();
	        nameWarnMsg.textContent = '이름을 입력해주세요.';
			nameWarnMsg.style.color = 'rgb(255, 107, 107)';
			nameWarnMsg.style.display = 'inline';
			return false;
		} else{
			return true;
		}
	} else {
		return true;
	}
}