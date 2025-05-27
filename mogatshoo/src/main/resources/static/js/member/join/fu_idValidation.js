function fu_idValidation(){
	
	let idInput = document.getElementById('memberId');
	let idWarnMsg = document.getElementById('idWarnMsg');
	
	if(idInput){
		idWarnMsg.textContent = '';
		
		let idVal = idInput.value.trim();
		
		if(idVal == '' || idVal.length == 0){
			idInput.focus();
			idWarnMsg.textContent = '아이디를 입력해주세요.';
			idWarnMsg.style.color = 'rgb(255, 107, 107)';
			return false;
		} else{
			return true;
		}
	} else {
		idCheck = true;
		return true;
	}
}