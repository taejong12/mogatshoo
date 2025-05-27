function fu_genderValidation(){
	
	let genderInputList = document.getElementsByName('memberGender');
	let genderWarnMsg = document.getElementById('genderWarnMsg');
	
	if(genderInputList.length > 1){
		genderWarnMsg.textContent = '';
		
		let genderSelected = false;
		let firstInput = null;
		
		for (let i = 0; i < genderInputList.length; i++) {
			// 첫 번째 요소 기억
		    if (i === 0) firstInput = genderInputList[i];
		    if (genderInputList[i].checked) {
		        genderSelected = true;
		        break;
		    }
		}
	
		if (!genderSelected) {
		    firstInput.focus();
			genderWarnMsg.textContent = '성별을 선택해주세요.';
			genderWarnMsg.style.color = 'rgb(255, 107, 107)';
		    return false;
		}else{
			return true;
		}
	} else {
		return true;
	}
}