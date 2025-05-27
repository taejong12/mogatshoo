function fu_findById(){
	let findIdform = document.findByIdForm;
	let emailInput = findIdform.memberEmail;
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
	.then(response => {
		if (!response.ok) {
	        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
	    }
		return response.json();
	})
	.then(data => {
		
		if (data.memberEmailCheck) {
			emailWarnMsg.textContent = "없는 이메일입니다.";
		} else {
			findIdform.method= "post";
			findIdform.action= "/member/findById";
			findIdform.submit();
		}
	})
	.catch(error => {
		console.error("아이디 찾기 이메일 확인 오류:", error);
	});
}