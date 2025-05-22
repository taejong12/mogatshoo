// 이메일 중복 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	
	let memberEmail = document.getElementById('memberEmail');
	let emailCheckMessage = document.getElementById('emailCheckMessage');
	let emailBtn = document.querySelector('.email-btn');
	
	if(memberEmail){
		// 이메일 입력 시
		memberEmail.addEventListener('input', function() {
			
			let memberEmailVal = memberEmail.value.trim();
			let emailWarn = document.querySelector('.warn-div.email');
			
			if (memberEmailVal === '') {
				emailCheckMessage.textContent = '';
				emailCheckMessage.style.display = 'none';
				emailBtn.disabled = true;
				return;
			}
			
			if(emailWarn){
				emailWarn.closest('.input-wrap').remove();
			}
			
			// 간단한 이메일 정규표현식
		    let emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			
			if (!emailRegex.test(memberEmailVal)) {
				emailCheckMessage.textContent = '이메일 주소가 정확한지 확인해 주세요.';
				emailCheckMessage.style.color = "red";
				emailCheckMessage.style.display = 'inline';
				return;
			}
			
			fetch("/member/emailCheck", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ memberEmail: memberEmailVal })
			})
			.then(response => response.json())
			.then(data => {
				if (data.memberEmailCheck) {
					emailCheckMessage.textContent = "사용 가능한 이메일입니다.";
					emailCheckMessage.style.color = "green";
					emailCheckMessage.style.display = 'inline';
					emailBtn.disabled = false;
					emailCheckMessage.classList.add('valid');
				} else {
					emailCheckMessage.textContent = "이미 사용 중인 이메일입니다.";
					emailCheckMessage.style.color = "red";
					emailCheckMessage.style.display = 'inline';
					emailBtn.disabled = true;
				}
			})
			.catch(error => {
				console.error("이메일 중복 확인 오류:", error);
				window.location.href = "/error/globalError";
			});
		});
	}
});