function fu_nickNameValidation(){
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameVal = nickNameInput.value.trim();
	
	if(nickNameVal == '' || nickNameVal.length == 0){
		nickNameInput.focus();
		
        let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
        
		let nickNameWarp = nickNameInput.closest('.input-wrap');
		
		let inputWrap = document.createElement('div');
		inputWrap.className = 'input-wrap d-flex align-items-center';
		
		let nickNameWarnDiv = document.createElement('div');
		nickNameWarnDiv.className = 'warn-div nickName';
		nickNameWarnDiv.textContent = '닉네임을 입력해주세요.';
		
		inputWrap.appendChild(spaceDiv);
		inputWrap.appendChild(nickNameWarnDiv);
		
		nickNameWarp.insertAdjacentElement('afterend', inputWrap);
		return false;
	} else{
		return true;
	}
}