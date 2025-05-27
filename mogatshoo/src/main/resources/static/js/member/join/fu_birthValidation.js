function fu_birthValidation(){
			
	let birthInput = document.getElementById('memberBirth');
	let birthWarnMsg = document.getElementById('birthWarnMsg');
	
	if(birthInput){
		birthWarnMsg.textContent = '';
		
		let birthVal = birthInput.value.trim();
		
		if(birthVal == '' || birthVal.length == 0){
			birthInput.focus();
			birthWarnMsg.textContent = '생년월일을 입력해주세요.';
			birthWarnMsg.style.color = 'rgb(255, 107, 107)';
			return false;
		} else{
			return true;
		}
	} else {
		return true;
	}
}