function fu_agreeVali(){
	let agreeIntegration = document.getElementById('agreeIntegration');
	let agreeInfo = document.getElementById('agreeInfo');
	let inteWarnMsg = document.getElementById('inteWarnMsg');
	let infoWarnMsg = document.getElementById('infoWarnMsg');
	let inteCheck = false;
	let infoCheck = false;
	
	inteWarnMsg.textContent = '';
	infoWarnMsg.textContent = '';
	
	if(!agreeIntegration.checked || agreeIntegration.value != 'Y'){
		inteWarnMsg.textContent = '모갓슈 통합 서비스 이용약관에 대한 내용 확인 후 동의해주세요.';
		agreeIntegration.focus();
		inteCheck = false;
	}else{
		inteCheck = true;
	}
	
	if(!agreeInfo.checked || agreeInfo.value != 'Y'){
		infoWarnMsg.textContent = '개인정보 수집 및 이용에 대한 내용 확인 후 동의해주세요.';
		agreeInfo.focus();
		infoCheck = false;
	}else{
		infoCheck = true;
	}
	
	if(infoCheck && inteCheck){
		return true;
	} else{
		return false;
	}
}