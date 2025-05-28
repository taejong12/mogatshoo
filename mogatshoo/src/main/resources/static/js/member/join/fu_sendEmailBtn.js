function fu_sendEmailBtn(){
	
	let memberEmail = document.getElementById('memberEmail');
	let memberEmailVal = memberEmail.value.trim();
	let sendEmailBtn = document.getElementById('sendEmailBtn');
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	let emailAuth = document.getElementById('emailAuth');
	let emailAuthBtn = document.getElementById('emailAuthBtn');
	let emailAuthWrap = document.getElementById('emailAuthWrap');
	let authWarnMsg = document.getElementById('authWarnMsg');
	
	emailWarnMsg.textContent = '';
	emailAuth.value = '';
	authWarnMsg.textContent = '';
	sendEmailBtn.disabled = true;
	emailAuth.disabled = false;
	emailAuthBtn.disabled = false;
	
	fetch("/member/sendEmail", {
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
		
		emailAuthWrap.style.display = "flex";
		emailWarnMsg.textContent = '이메일을 발송하였습니다.';
		emailWarnMsg.style.color = 'rgb(129, 199, 132)';
		emailWarnMsg.style.display = 'inline';
		
		// 타이머 3분 (180초)
	    let timer = document.getElementById('timer');
		timer.textContent = '';
		clearInterval(timerInterval);
	    let timeLeft = 180;
		
		let startTimer = () => {
			timerInterval = setInterval(() => {
				let minutes = String(Math.floor(timeLeft / 60)).padStart(2, '0');
				let seconds = String(timeLeft % 60).padStart(2, '0');
				timer.textContent = minutes+":"+seconds;
				timer.style.display = 'inline';

				if (timeLeft <= 0) {
					clearInterval(timerInterval);
					timer.textContent = "인증시간초과";
					emailAuth.value = '';
					emailAuth.disabled = true;
					emailAuthBtn.disabled = true;
				}

				timeLeft--;
			}, 1000);
		};
		
		// 중복 리스너 제거
	    emailAuthBtn.replaceWith(emailAuthBtn.cloneNode(true));
	    emailAuthBtn = document.getElementById('emailAuthBtn');
		
		emailAuthBtn.addEventListener("click", function () {
			let emailAuthCode = emailAuth.value.trim();
			
	        if (!emailAuthCode) {
	        	emailAuth.focus();
	    		authWarnMsg.textContent = '인증번호를 입력해주세요.';
	    		authWarnMsg.style.color = "rgb(255, 107, 107)";
				authWarnMsg.style.display = 'block';
	            return;
	        }

	        fetch("/member/emailAuthCodeConfirm", {
	            method: "POST",
	            headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ emailAuthCode : emailAuthCode })
	        })
			.then(response => {
				if (!response.ok) {
			        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
			    }
				return response.json();
			})
	        .then(data => {
	        	
	            authWarnMsg.innerHTML = data.msg;
				authWarnMsg.style.display = 'block';
				
	            if (data.result === true) {
	            	emailAuth.readOnly  = true;
	            	emailAuthBtn.disabled = true;
	            	memberEmail.readOnly  = true;
	            	emailAuthCheck = true;
	                timer.remove();
	                authWarnMsg.style.color = "rgb(129, 199, 132)";
	            } else {
	            	emailAuth.focus();
					authWarnMsg.style.color = "rgb(255, 107, 107)";
					
					if(data.authTryCount >= 5){
						timer.textContent = '';
						timer.style.display = 'none';
						clearInterval(timerInterval);
						emailAuth.disabled = true;
						emailAuthBtn.disabled = true;
					}
	            } 
	        })
	        .catch(error => {
	            console.error("메일 인증 오류 발생:", error);
	        });
	    });
	    
    	startTimer();
	})
	.catch(error => {
		console.error("이메일 인증번호 전송 오류:", error);
	});
}