// 비밀번호 확인 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	let memberPwdCheck = document.getElementById('memberPwdCheck');
	let memberPwd = document.getElementById('memberPwd');
	let pwdCheckWarnMsg = document.getElementById('pwdCheckWarnMsg');
	
	if(memberPwdCheck){
		
		pwdCheckWarnMsg.textContent = '';
		
		// 비밀번호 확인 입력 시
		memberPwdCheck.addEventListener('input', function() {
			let pwdVal = memberPwd.value.trim();
			let pwdCheckVal = memberPwdCheck.value.trim();
			
			if (pwdCheckVal === '' || pwdVal === '') {
				pwdCheckWarnMsg.textContent = '';
				pwdCheckWarnMsg.style.color = 'rgb(255, 107, 107)';
				pwdCheckWarnMsg.style.display = 'none';
				pwdCheckEqual = false;
				return;
			}
			
			if (pwdVal === pwdCheckVal) {
				pwdCheckWarnMsg.textContent = '비밀번호가 일치합니다.';
				pwdCheckWarnMsg.style.color = 'rgb(129, 199, 132)';
				pwdCheckWarnMsg.style.display = 'inline';
				pwdCheckEqual = true;
			} else {
				pwdCheckWarnMsg.textContent = '비밀번호가 일치하지 않습니다.';
				pwdCheckWarnMsg.style.color = 'rgb(255, 107, 107)';
				pwdCheckWarnMsg.style.display = 'inline';
				pwdCheckEqual = false;
			}
		});
	} else {
		pwdCheckEqual = true;
	}
});