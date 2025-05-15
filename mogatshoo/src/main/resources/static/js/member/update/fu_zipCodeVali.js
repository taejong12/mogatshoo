function fu_zipCodeVali(){
	
	let zipCodeInput = document.getElementById('memberZipcode');
	let zipCodeWarnMsg = document.getElementById('zipCodeWarnMsg');
	
	zipCodeWarnMsg.textContent = '';
	
	if(zipCodeInput.value == "" || zipCodeInput.value.length == 0){
		zipCodeInput.focus();
		zipCodeWarnMsg.textContent = '우편번호를 입력해주세요.';
		return false;
	} else {
		return true;
	}
}