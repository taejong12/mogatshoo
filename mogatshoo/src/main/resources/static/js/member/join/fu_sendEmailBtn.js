function fu_sendEmailBtn(){
	
	let memberEmail = document.getElementById('memberEmail');
	let memberEmailVal = memberEmail.value.trim();
	let sendEmailBtn = document.getElementById('sendEmailBtn');
	let emailWarnMsg = document.getElementById('emailWarnMsg');
				
	emailWarnMsg.textContent = '';
	sendEmailBtn.disabled = true;
	
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
		
	    let emailAuthWrap = document.getElementById('emailAuthWrap');
		
		emailAuthWrap.style.display = "flex";
		
		let emailWarnMsg = document.getElementById('emailWarnMsg');
			
		emailWarnMsg.textContent = '이메일을 발송하였습니다.';
		emailWarnMsg.style.color = 'rgb(129, 199, 132)';
		
	    let timer = document.getElementById('timer');
	    let emailAuth = document.getElementById('emailAuth');
	    let emailAuthBtn = document.getElementById('emailAuthBtn');
		let authWarnMsg = document.getElementById('authWarnMsg');
	    
		emailAuth.value = '';
		timer.textContent = '';
		authWarnMsg.textContent = '';
		clearInterval(timerInterval);
		
		// 3분 (180초)
	    let timeLeft = 180;
		
		let startTimer = () => {
			timerInterval = setInterval(() => {
				let minutes = String(Math.floor(timeLeft / 60)).padStart(2, '0');
				let seconds = String(timeLeft % 60).padStart(2, '0');
				timer.textContent = minutes+":"+seconds;

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
		
		emailAuthBtn.addEventListener("click", function () {
			let emailAuthCode = emailAuth.value.trim();
			
	        if (!emailAuthCode) {
	        	emailAuth.focus();
	    		authWarnMsg.textContent = '인증번호를 입력해주세요.';
	    		authWarnMsg.style.color = "rgb(255, 107, 107)";
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
	        	
	            authWarnMsg.textContent = data.msg;

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