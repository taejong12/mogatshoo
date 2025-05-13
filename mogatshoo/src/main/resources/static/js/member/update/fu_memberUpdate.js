function fu_memberUpdate(){
	let updateForm = document.memberUpdateForm;
	let nameInput = updateForm.memberName;
	let nameWarnMsg = document.getElementById('nameWarnMsg');
	
	nameWarnMsg.textContent = '';
	
	if(nameInput.value == "" || nameInput.value.length == 0){
		updateForm.focus();
		nameWarnMsg.textContent = '이름을 입력해주세요.';
		return;
	}
	
	updateForm.method="post";
	updateForm.action="/member/update";
	updateForm.submit();
}
