// 비밀번호 확인 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	let memberPwdCheck = document.getElementById('memberPwdCheck');
	let memberPwd = document.getElementById('memberPwd');
	let pwdCheckMessage = document.getElementById('pwdCheckMessage');
	
	// 비밀번호 확인 입력 시
	memberPwdCheck.addEventListener('input', function() {
		let pwdVal = memberPwd.value.trim();
		let pwdCheckVal = memberPwdCheck.value.trim();
		let pwdCheckWarn = document.querySelector('.warn-div.pwdCheck');
		
		if (pwdCheckVal === '' || pwdVal === '') {
			pwdCheckMessage.textContent = '';
			pwdCheckMessage.style.display = 'none';
			pwdCheckEqual = false;
			return;
		}
		
		if(pwdCheckWarn){
			pwdCheckWarn.closest('.input-wrap').remove();
		}
		
		if (pwdVal === pwdCheckVal) {
			pwdCheckMessage.textContent = '비밀번호가 일치합니다.';
			pwdCheckMessage.style.color = 'green';
			pwdCheckMessage.style.display = 'inline';
			pwdCheckEqual = true;
		} else {
			pwdCheckMessage.textContent = '비밀번호가 일치하지 않습니다.';
			pwdCheckMessage.style.color = 'red';
			pwdCheckMessage.style.display = 'inline';
			pwdCheckEqual = false;
		}
	});
});