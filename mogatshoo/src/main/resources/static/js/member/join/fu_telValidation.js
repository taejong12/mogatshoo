function fu_telValidation(){
	
	let telInput = document.getElementById('memberTel');
	let telWarnMsg = document.getElementById('telWarnMsg');
	
	
	if(telInput){
		telWarnMsg.textContent = '';
		
		let telVal = telInput.value.trim();
		
		if(telVal == '' || telVal.length == 0){
			telInput.focus();
			telWarnMsg.textContent = '전화번호를 입력해주세요.';
			return false;
		} else{
			
			let telPattern = /^01[016789]\d{7,8}$/;
	
		    if (!telPattern.test(telVal)) {
		        telInput.focus();
				telWarnMsg.textContent = '전화번호를 올바르게 입력하세요.';
		        return false;
		    }
			return true;
		}
	} else {
		return true;
	}
}