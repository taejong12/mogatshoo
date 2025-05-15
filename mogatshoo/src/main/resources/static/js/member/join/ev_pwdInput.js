// 비밀번호 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	let memberPwd = document.getElementById('memberPwd');
	let memberPwdCheck = document.getElementById('memberPwdCheck');
	let pwdCheckMessage = document.getElementById('pwdCheckMessage');
	let eight = document.querySelector('.eight');
	let special = document.querySelector('.special');
	let continu = document.querySelector('.continu');
	
	if(memberPwd){
		// 비밀번호 입력 시
		memberPwd.addEventListener('input', function() {
			let pwdVal = memberPwd.value.trim();
			let pwdCheckVal = memberPwdCheck.value.trim();
			let pwdWarn = document.querySelector('.warn-div.pwd');
			
			if (pwdVal === '' || pwdVal.length == 0) {
				eight.style.color = 'gray';
				special.style.color = 'gray';
				continu.style.color = 'gray';
				memberPwdCheck.value = '';
				pwdCheckMessage.textContent = '';
				pwdInputCheck = false;
				pwdCheckEqual = false;
				return;
			}
			
			if(pwdWarn){
				pwdWarn.closest('.input-wrap').remove();
			}
			
			if(pwdCheckVal != '' || !(pwdCheckVal.length == 0)){
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
			}
			
			// 1. 8자리 이상
			let isEight = pwdVal.length >= 8;
			
			// 2. 영문자+숫자, 대문자 1개 이상, 특수문자 1개 이상 포함
			let hasLetter = /[a-zA-Z]/.test(pwdVal);
			let hasUpper = /[A-Z]/.test(pwdVal);
			let hasDigit = /[0-9]/.test(pwdVal);
			let hasSpecial = /[^a-zA-Z0-9]/.test(pwdVal);
			let isSpecialValid = hasLetter && hasUpper && hasDigit && hasSpecial;
	
			// 3. 연속된 숫자 (예: 1234, 4567 등) 방지
			let isSequential = false;
			for (let i = 0; i < pwdVal.length - 2; i++) {
				let a = pwdVal.charCodeAt(i);
				let b = pwdVal.charCodeAt(i + 1);
				let c = pwdVal.charCodeAt(i + 2);
				if (b === a + 1 && c === b + 1) {
					isSequential = true;
					break;
				}
			}
			let isContinuValid = !isSequential;
			
			// 스타일 업데이트
			eight.style.color = isEight ? 'green' : 'red';
			special.style.color = isSpecialValid ? 'green' : 'red';
			continu.style.color = isContinuValid ? 'green' : 'red';
	
			// 모든 조건 만족하면 true
			pwdInputCheck = isEight && isSpecialValid && isContinuValid;
			
		});
	} else{
		pwdInputCheck = true;
	}
});