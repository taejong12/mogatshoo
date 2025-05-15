function fu_nameVali(){
	
	let nameInput = document.getElementById('memberName');
	let nameWarnMsg = document.getElementById('nameWarnMsg');
	
	nameWarnMsg.textContent = '';
	
	if(nameInput.value == "" || nameInput.value.length == 0){
		nameInput.focus();
		nameWarnMsg.textContent = '이름을 입력해주세요.';
		return false;
	} else {
		return true;
	}
}