function fu_findByPwd(){
	let form = document.findByPwdForm;
	let idInput = form.memberId;
	let emailInput = form.memberEmail;
	let idVal = idInput.value.trim();
	let emailVal = emailInput.value.trim();
	let emailWarnMsg = document.getElementById('emailWarnMsg');
	let idWarnMsg = document.getElementById('idWarnMsg');
	let findByPwdError = document.getElementById('findByPwdError');

	idWarnMsg.textContent = '';
	emailWarnMsg.textContent = '';
	findByPwdError.textContent = '';
	
	let idCheck = false;
	let emailCheck = false;
	
	if(emailVal == "" || emailVal.length == 0){
		emailInput.focus();
		emailWarnMsg.textContent = '이메일을 입력해주세요.';
		emailCheck = false;
	} else {
		emailCheck = true;
	}
	
	if(idVal == "" || idVal.length == 0){
		idInput.focus();
		idWarnMsg.textContent = '아이디를 입력해주세요.';
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
		.then(response => response.json())
		.then(data => {
			if (data.idAndEmailCheck) {
				findByPwdError.textContent = "일치하는 회원이 없습니다.";
			} else {
				
				let sendEmailBtn = document.getElementById('sendEmailBtn');
				sendEmailBtn.disabled = true;
				
				idInput.readOnly = true;
				emailInput.readOnly = true;
				
				findByPwdError.textContent = "";
				
				let emailAuthDiv = document.getElementById('emailAuthDiv');
							
				let emailAuthLabel = document.createElement('label');
				emailAuthLabel.htmlFor  = 'emailAuth';
				emailAuthLabel.classList.add('form-label');
				emailAuthLabel.textContent = '인증번호';
				
				let emailAuthInput = document.createElement('input');
				emailAuthInput.type = 'text';
				emailAuthInput.classList.add('form-control');
				emailAuthInput.id = 'emailAuth';
				emailAuthInput.name = 'emailAuth';
				emailAuthInput.placeholder = '인증번호 입력';

				let timerDiv = document.createElement('div');
				timerDiv.classList.add('timer-div');
				
				let authWarnMsg = document.createElement('div');
				authWarnMsg.id = 'authWarnMsg';
				authWarnMsg.style.color = 'red';
				authWarnMsg.classList.add('text-center', 'mb-3');
				
				emailAuthDiv.appendChild(emailAuthLabel);
				emailAuthDiv.appendChild(emailAuthInput);
				emailAuthDiv.appendChild(timerDiv);
				emailAuthDiv.appendChild(authWarnMsg);
				
				let authBtnDiv = document.getElementById('authBtn');
				
				let authBtn = document.createElement('button');
				authBtn.type = 'button';
				authBtn.classList.add('btn', 'btn-white', 'btn-lg', 'border', 'border-dark');
				authBtn.textContent = '인증하기';
				
				authBtnDiv.appendChild(authBtn);
				
				// 3분 (180초)
			    let timeLeft = 180;
			    
				let startTimer = () => {
					let timerInterval = setInterval(() => {
						let minutes = String(Math.floor(timeLeft / 60)).padStart(2, '0');
						let seconds = String(timeLeft % 60).padStart(2, '0');
						timerDiv.textContent = minutes+":"+seconds;

						if (timeLeft <= 0) {
							clearInterval(timerInterval);
							timerDiv.textContent = "인증시간초과";
							emailAuthInput.value = '';
							emailAuthInput.disabled = true;
							authBtn.disabled = true;
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
				.then(response => response.json())
				.then(data => {
					
					if (data.result === true) {
						
						authBtn.addEventListener("click", function(){
							
							let emailAuthCode = document.getElementById('emailAuth').value.trim();
							
							authWarnMsg.textContent = '';
							
							fetch("/member/emailAuthCodeConfirm", {
					            method: "POST",
					            headers: {
									"Content-Type": "application/json"
								},
								body: JSON.stringify({ emailAuthCode : emailAuthCode })
					        })
					        .then(response => response.json())
					        .then(data => {
								
								if (data.result === true) {
									form.method= "post";
									form.action= "/member/pwdUpdateForm";
									form.submit();
								} else {
									authWarnMsg.textContent = '인증번호가 일치하지 않습니다.';
								}
							})
							.catch(error => {
								console.error("비밀번호 찾기 인증번호 전송 오류:", error);
								window.location.href = "/error/globalError";
							});
						})
					}
				})
				.catch(error => {
					console.error("비밀번호 찾기 인증번호 전송 오류:", error);
					window.location.href = "/error/globalError";
				});
			}
		})
		.catch(error => {
			console.error("비밀번호 찾기 회원 확인 오류:", error);
			window.location.href = "/error/globalError";
		});
		
	}
	
}
