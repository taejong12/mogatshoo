function fu_findByPwd(){
	let findPwdform = document.findByPwdForm;
	let idInput = findPwdform.memberId;
	let idVal = idInput.value.trim();
	let emailInput = findPwdform.memberEmail;
	let emailVal = emailInput.value.trim();
	let idWarnMsg = document.getElementById('idWarnMsg');
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	let findPwdError = document.getElementById('findPwdError');

	idWarnMsg.textContent = '';
	emailWarnMsg.textContent = '';
	findPwdError.textContent = '';
	idWarnMsg.style.display = 'none';
	emailWarnMsg.style.display = 'none';
	findPwdError.style.display = 'none';
	
	let idCheck = false;
	let emailCheck = false;
	
	if(emailVal == "" || emailVal.length == 0){
		emailInput.focus();
		emailWarnMsg.textContent = '이메일을 입력해주세요.';
		emailWarnMsg.style.display = 'inline';
		emailCheck = false;
	} else {
		emailCheck = true;
	}
	
	if(idVal == "" || idVal.length == 0){
		idInput.focus();
		idWarnMsg.textContent = '아이디를 입력해주세요.';
		idWarnMsg.style.display = 'inline';
		idCheck = false;
	} else {
		idCheck = true;
	}
	
	if(idCheck && emailCheck){
		
		fetch("/member/idAndEmailCheck", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({ memberId: idVal, memberEmail: emailVal })
		})
		.then(response => {
			if (!response.ok) {
		        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
		    }
			return response.json();
		})
		.then(data => {
			
			if (data.idAndEmailCheck) {
				findPwdError.textContent = "일치하는 회원이 없습니다.";
				findPwdError.style.display = 'inline';
			} else {
				
				let sendEmailBtn = document.getElementById('sendEmailBtn');
				sendEmailBtn.disabled = true;
				
				idInput.readOnly = true;
				emailInput.readOnly = true;
				
				let emailAuthWrap = document.getElementById('emailAuthWrap');
				
				emailAuthWrap.style.display = "flex";
				
				let emailAuth = document.getElementById('emailAuth');
				
				emailWarnMsg.textContent = '이메일을 발송하였습니다.';
				emailWarnMsg.style.color = 'rgb(129, 199, 132)';
				emailWarnMsg.style.display = 'inline';
				
				let emailAuthBtn = document.getElementById('emailAuthBtn');
				let authWarnMsg = document.getElementById('authWarnMsg');
				let timer = document.getElementById('timer');
				
				emailAuth.value = '';
				timer.textContent = '';
				authWarnMsg.textContent = '';
				
				// 3분 (180초)
			    let timeLeft = 180;
			    
				let startTimer = () => {
					let timerInterval = setInterval(() => {
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
				
				startTimer();
				
				fetch("/member/sendEmail", {
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
					
					if (data.result === true) {
						
						emailAuthBtn.addEventListener("click", function(){
							
							let emailAuthCode = emailAuth.value.trim();
							
							if (!emailAuthCode) {
					        	emailAuth.focus();
					    		authWarnMsg.textContent = '인증번호를 입력해주세요.';
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
								
								if (data.result === true) {
									findPwdform.method= "post";
									findPwdform.action= "/member/pwdUpdateForm";
									findPwdform.submit();
								} else {
									emailAuth.focus();
									authWarnMsg.innerHTML = data.msg;
									authWarnMsg.style.display = 'block';
									
									if(data.authTryCount >= 5){
										timer.remove();
										emailAuth.disabled = true;
										emailAuthBtn.disabled = true;
									}
									
								}
							})
							.catch(error => {
								console.error("비밀번호 찾기 인증번호 전송 오류:", error);
							});
						})
					}
				})
				.catch(error => {
					console.error("비밀번호 찾기 인증번호 전송 오류:", error);
				});
			}
		})
		.catch(error => {
			console.error("비밀번호 찾기 회원 확인 오류:", error);
		});
		
	}
	
}
