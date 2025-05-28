function fu_nickNameValidation(){
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameWarnMsg = document.getElementById('nickNameWarnMsg');
	
	if(nickNameInput){
		nickNameWarnMsg.textContent = '';
		
		let nickNameVal = nickNameInput.value.trim();
		
		if(nickNameVal == '' || nickNameVal.length == 0){
			nickNameInput.focus();
	        nickNameWarnMsg.textContent = '닉네임을 입력해주세요.';
			nickNameWarnMsg.style.color = 'rgb(255, 107, 107)';
			nickNameWarnMsg.style.display = 'inline';
			return false;
		} else{
			return true;
		}
	} else {
		nickNameCheck = true;
		return true;
	}
}