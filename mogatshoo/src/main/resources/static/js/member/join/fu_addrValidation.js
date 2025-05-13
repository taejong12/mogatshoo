function fu_addrValidation(){
	let zipCodeInput = document.getElementById('memberZipcode');
	let addr1Input = document.getElementById('memberAddress1');
	let addr2Input = document.getElementById('memberAddress2');
	
	let zipCheck = false;
	let addr1Check = false;
	let addr2Check = false;
	
	if(addr2Input){
		let addr2Val = addr2Input.value.trim();
		
		if(addr2Val == '' || addr2Val.length == 0){
			addr2Input.focus();
					
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let addr2Warp = addr2Input.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let addr2WarnDiv = document.createElement('div');
			addr2WarnDiv.className = 'warn-div addr2';
			addr2WarnDiv.textContent = '상세주소를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(addr2WarnDiv);
			
			addr2Warp.insertAdjacentElement('afterend', inputWrap);
			
			addr2Check=false;
		} else{
			addr2Check=true;
		}
	} else{
		addr2Check=true;
	}
	
	if(addr1Input){
		let addr1Val = addr1Input.value.trim();
		
		if(addr1Val == '' || addr1Val.length == 0){
			addr1Input.focus();
					
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let addr1Warp = addr1Input.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let addr1WarnDiv = document.createElement('div');
			addr1WarnDiv.className = 'warn-div addr1';
			addr1WarnDiv.textContent = '기본주소를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(addr1WarnDiv);
			
			addr1Warp.insertAdjacentElement('afterend', inputWrap);
			
			addr1Check=false;
		} else{
			addr1Check=true;
		}
	} else{
		addr1Check=true;
	}
	
	if(zipCodeInput){
		let zipCodeVal = zipCodeInput.value.trim();
			
		if(zipCodeVal == '' || zipCodeVal.length == 0){
			zipCodeInput.focus();
			
	        let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let zipCodeWarp = zipCodeInput.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let zipCodeWarnDiv = document.createElement('div');
			zipCodeWarnDiv.className = 'warn-div zipCode';
			zipCodeWarnDiv.textContent = '우편번호를 입력해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(zipCodeWarnDiv);
			
			zipCodeWarp.insertAdjacentElement('afterend', inputWrap);
			
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