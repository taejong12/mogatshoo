function fu_findById(){
	let form = document.findByIdForm;
	let emailInput = form.memberEmail;
	let emailVal = emailInput.value.trim();
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	
	emailWarnMsg.textContent = '';
	
	if(emailVal == "" || emailVal.length == 0){
		emailInput.focus();
		emailWarnMsg.textContent = '이메일을 입력해주세요.';
		return;
	}
	
	fetch("/member/findByIdEmailCheck", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({ memberEmail: emailVal })
	})
	.then(response => response.json())
	.then(data => {
		
		if (data.memberEmailCheck) {
			emailWarnMsg.textContent = "없는 이메일입니다.";
			emailWarnMsg.style.color = "red";
			emailWarnMsg.style.display = 'inline';
		} else {
			form.method= "post";
			form.action= "/member/findById";
			form.submit();
		}
	})
	.catch(error => {
		console.error("아이디 찾기 이메일 확인 오류:", error);
		window.location.href = "/error/globalError";
	});
}