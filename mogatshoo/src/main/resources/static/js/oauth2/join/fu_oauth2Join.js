function fu_oauth2Join(){
	
	let form = document.oauth2JoinForm;
	
	// 경고 문구 초기화
	fu_warnCheck();
	
	// 유효성 검사
	let infoCheckValidation = fu_infoCheckValidation();
	let emailValidation = fu_emailValidation();
	let birthValidation = fu_birthValidation();
	let telValidation = fu_telValidation();
	let nameValidation = fu_nameValidation();
	let idValidation = fu_idValidation();
	
	if(idValidation	&& nameValidation && telValidation 
		&& birthValidation && emailValidation && infoCheckValidation){
		form.method= "post";
		form.action = "/member/join";
		form.submit();
	}
}
	
