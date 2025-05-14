function fu_genderValidation(){
	
	let genderInputList = document.getElementsByName('memberGender');
	
	if(genderInputList.length > 1){
		let genderSelected = false;
		let firstInput = null;
		
		for (let i = 0; i < genderInputList.length; i++) {
		    if (i === 0) firstInput = genderInputList[i]; // 첫 번째 요소 기억
		    if (genderInputList[i].checked) {
		        genderSelected = true;
		        break;
		    }
		}
	
		if (!genderSelected) {
		    firstInput.focus();
			
			let spaceDiv = document.createElement('div');
			spaceDiv.style.width = "110px";
	        
			let genderWarp = firstInput.closest('.input-wrap');
			
			let inputWrap = document.createElement('div');
			inputWrap.className = 'input-wrap d-flex align-items-center';
			
			let genderWarnDiv = document.createElement('div');
			genderWarnDiv.className = 'warn-div gender';
			genderWarnDiv.textContent = '성별을 선택해주세요.';
			
			inputWrap.appendChild(spaceDiv);
			inputWrap.appendChild(genderWarnDiv);
			
			genderWarp.insertAdjacentElement('afterend', inputWrap);
		    return false;
		}else{
			return true;
		}
	} else {
		return true;
	}
}