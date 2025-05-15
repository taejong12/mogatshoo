function fu_memberUpdate(){
	let updateForm = document.memberUpdateForm;
	
	let telVali = fu_telVali();
	let addr2Vali = fu_addr2Vali();
	let addr1Vali = fu_addr1Vali();
	let zipCodeVali = fu_zipCodeVali();
	let nameVali = fu_nameVali();
	let nickNameVali = fu_nickNameVali();
	
	if(!nickNameCheck){
		updateForm.memberNickName.focus();
		let nickNameWarnMsg = document.getElementById("nickNameWarnMsg");
		nickNameWarnMsg.textContent = '이미 사용 중인 닉네임입니다.';
	}
	
	if(nickNameVali && nameVali && zipCodeVali
		&& addr1Vali && addr2Vali && telVali
		&& nickNameCheck
	){
		updateForm.method="post";
		updateForm.action="/member/update";
		updateForm.submit();
	}
}
