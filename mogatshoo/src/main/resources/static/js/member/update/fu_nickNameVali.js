function fu_nickNameVali(){
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameVal = nickNameInput.value.trim();
	let nickNameWarnMsg = document.getElementById('nickNameWarnMsg');
	let nickChk = document.getElementById('nickChk').value.trim();
	
	nickNameWarnMsg.textContent = '';
	
	if(nickNameInput.value == "" || nickNameInput.value.length == 0){
		nickNameInput.focus();
		nickNameWarnMsg.textContent = '닉네임을 입력해주세요.';
		return false;
	} else {
		
		if(nickNameVal == nickChk){
			nickNameCheck = true;
		}
		return true;
	}
}