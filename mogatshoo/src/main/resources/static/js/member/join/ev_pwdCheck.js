// 비밀번호 확인 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	let memberPwdCheck = document.getElementById('memberPwdCheck');
	let memberPwd = document.getElementById('memberPwd');
	let pwdCheckMsg = document.getElementById('pwdCheckMsg');
	
	if(memberPwdCheck){
		
		pwdCheckMsg.textContent = '';
		
		// 비밀번호 확인 입력 시
		memberPwdCheck.addEventListener('input', function() {
			let pwdVal = memberPwd.value.trim();
			let pwdCheckVal = memberPwdCheck.value.trim();
			
			if (pwdCheckVal === '' || pwdVal === '') {
				pwdCheckMsg.textContent = '';
				pwdCheckMsg.style.color = 'rgb(255, 107, 107)';
				pwdCheckEqual = false;
				return;
			}
			
			if (pwdVal === pwdCheckVal) {
				pwdCheckMsg.textContent = '비밀번호가 일치합니다.';
				pwdCheckMsg.style.color = 'rgb(129, 199, 132)';
				pwdCheckEqual = true;
			} else {
				pwdCheckMsg.textContent = '비밀번호가 일치하지 않습니다.';
				pwdCheckMsg.style.color = 'rgb(255, 107, 107)';
				pwdCheckEqual = false;
			}
		});
	} else {
		pwdCheckEqual = true;
	}
});