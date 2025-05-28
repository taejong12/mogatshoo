// 이메일 중복 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	
	let memberEmail = document.getElementById('memberEmail');
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	let sendEmailBtn = document.getElementById('sendEmailBtn');
	
	if(memberEmail){
		
		sendEmailBtn.disabled = true;
		emailWarnMsg.textContent = '';
		
		memberEmail.addEventListener('input', function() {
			
			let memberEmailVal = memberEmail.value.trim();
			
			if (memberEmailVal === '' || memberEmailVal.length == 0) {
				emailWarnMsg.textContent = '';
				emailWarnMsg.style.display = 'none';
				emailWarnMsg.style.color = "rgb(255, 107, 107)";
				emailCheck = false;
				return;
			}
			
			// 간단한 이메일 정규표현식
		    let emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			
			if (!emailRegex.test(memberEmailVal)) {
				emailWarnMsg.textContent = '이메일 주소가 정확한지 확인해 주세요.';
				emailWarnMsg.style.color = "rgb(255, 107, 107)";
				emailWarnMsg.style.display = 'inline';
				sendEmailBtn.disabled = true;
				return;
			}
			
			fetch("/member/emailCheck", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ memberEmail: memberEmailVal })
			})
			.then(response => {
				if (!response.ok) {
			        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
			    }
				return response.json();
			})
			.then(data => {
				if (data.memberEmailCheck) {
					emailWarnMsg.textContent = "사용 가능한 이메일입니다.";
					emailWarnMsg.style.color = "rgb(129, 199, 132)";
					emailWarnMsg.style.display = 'inline';
					sendEmailBtn.disabled = false;
					emailCheck = true;
				} else {
					emailWarnMsg.textContent = "이미 사용 중인 이메일입니다.";
					emailWarnMsg.style.color = "rgb(255, 107, 107)";
					emailWarnMsg.style.display = 'inline';
					sendEmailBtn.disabled = true;
					emailCheck = false;
				}
			})
			.catch(error => {
				console.error("이메일 중복 확인 오류:", error);
			});
		});
	} else{
		emailCheck = true;
	}
});