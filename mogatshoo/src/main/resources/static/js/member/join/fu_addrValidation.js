function fu_addrValidation(){
	let zipCodeInput = document.getElementById('memberZipcode');
	let addr1Input = document.getElementById('memberAddress1');
	let addr2Input = document.getElementById('memberAddress2');
	let zipCodeWarnMsg = document.getElementById('zipCodeWarnMsg');
	let addr1WarnMsg = document.getElementById('addr1WarnMsg');
	let addr2WarnMsg = document.getElementById('addr2WarnMsg');
	
	let zipCheck = false;
	let addr1Check = false;
	let addr2Check = false;
	
	if(addr2Input){
		addr2WarnMsg.textContent = '';
		
		let addr2Val = addr2Input.value.trim();
		
		if(addr2Val == '' || addr2Val.length == 0){
			addr2Input.focus();
			addr2WarnMsg.textContent = '상세주소를 입력해주세요.';
			addr2WarnMsg.style.color = 'rgb(255, 107, 107)';
			addr2WarnMsg.style.display = 'inline';
			addr2Check=false;
		} else{
			addr2Check=true;
		}
	} else{
		addr2Check=true;
	}
	
	if(addr1Input){
		addr1WarnMsg.textContent = '';
		
		let addr1Val = addr1Input.value.trim();
		
		if(addr1Val == '' || addr1Val.length == 0){
			addr1Input.focus();
			addr1WarnMsg.textContent = '기본주소를 입력해주세요.';
			addr1WarnMsg.style.color = 'rgb(255, 107, 107)';
			addr1WarnMsg.style.display = 'inline';
			addr1Check=false;
		} else{
			addr1Check=true;
		}
	} else{
		addr1Check=true;
	}
	
	if(zipCodeInput){
		zipCodeWarnMsg.textContent = '';
		
		let zipCodeVal = zipCodeInput.value.trim();
			
		if(zipCodeVal == '' || zipCodeVal.length == 0){
			zipCodeInput.focus();
			zipCodeWarnMsg.textContent = '우편번호를 입력해주세요.';
			zipCodeWarnMsg.style.color = 'rgb(255, 107, 107)';
			zipCodeWarnMsg.style.display = 'inline';
			zipCheck=false;
		} else{
			zipCheck=true;
		}
	} else {
		zipCheck=true;
	}
	
	if(zipCheck && addr1Check && addr2Check){
		return true;
	}else{
		return false;
	}
}