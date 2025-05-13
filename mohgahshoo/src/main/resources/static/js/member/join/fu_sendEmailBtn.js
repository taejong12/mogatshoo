function fu_sendEmailBtn(){
	
	let memberEmailVal = document.getElementById('memberEmail').value.trim();
	let emailAuth = document.getElementById('emailAuth');
	let timerWarn = document.querySelector('.warn-div.timer');
	let authWarn = document.querySelector('.warn-div.auth');
	let emailAuthWarn = document.querySelector('.warn-div.emailAuth');
	let sendEmailBtn = document.querySelector('.email-btn');
	let emailWarn = document.querySelector('.warn-div.email');
	
	sendEmailBtn.disabled = true;
	sendEmailBtn.style.backgroundColor = '#f0f0f0';
	
	if(emailAuth){
		emailAuth.closest('.input-wrap').remove();
	}
	
	if(timerWarn){
		timerWarn.closest('.input-wrap').remove();
	}
	
	if(authWarn){
		authWarn.closest('.input-wrap').remove();
	}
	
	if(emailAuthWarn){
		emailAuthWarn.closest('.input-wrap').remove();
	}
	
	if(emailWarn){
		emailWarn.closest('.input-wrap').remove();
	}
	
	fetch("/member/sendEmail", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({ memberEmail: memberEmailVal })
	})
	.then(response => response.json())
	.then(data => {
		
		let newDiv = document.createElement('div');
		
		let authWrap = document.createElement('div');
		authWrap.className = 'input-wrap d-flex align-items-center';

	    let authLabel = document.createElement('label');
		authLabel.style.width = "110px";
	    authLabel.setAttribute('for', 'emailAuth');
	    authLabel.textContent = '인증번호';

	    let authInput = document.createElement('input');
	    authInput.type = 'text';
	    authInput.id = 'emailAuth';
	    authInput.name = 'emailAuth';
	    authInput.placeholder = '인증번호 입력';
	    authInput.className = 'flex-fill';

	    let authBtn = document.createElement('button');
	    authBtn.className = 'email-auth-btn text-nowrap bg-white border border-black px-3 py-2 d-flex align-items-center justify-content-center';
	    authBtn.style.width = "130px";
	    authBtn.style.marginLeft  = '20px';
	    authBtn.type = 'button';
	    authBtn.textContent = '확인';
	    
	    let timerWrap = document.createElement('div');
	    timerWrap.className = 'input-wrap d-flex align-items-center';

	    let spaceDiv = document.createElement('div');
		spaceDiv.style.width = "110px";
	    
	    let timerDiv = document.createElement('div');
	    timerDiv.className = 'warn-div timer';
	    
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
					authInput.value = '';
					authInput.disabled = true;
					authBtn.disabled = true;
				}

				timeLeft--;
			}, 1000);
		};
		
		// 인증번호 입력 시
		authInput.addEventListener('input', function() {
			
			let emailAuthWarn = document.querySelector('.warn-div.emailAuth');
			
			if(emailAuthWarn){
				emailAuthWarn.closest('.input-wrap').remove();
			}
		});
		
		authBtn.addEventListener("click", function () {
			let emailAuthInput = document.getElementById("emailAuth");
			let memberEmail = document.getElementById("memberEmail");
			let emailAuthCode = emailAuthInput.value.trim();
			let authWarn = document.querySelector('.warn-div.auth');
			
			if(authWarn){
				authWarn.closest('.input-wrap').remove();
			}
			
	        if (!emailAuthCode) {
	        	emailAuthInput.focus();
	        	
	        	let spaceDiv = document.createElement('div');
	    		spaceDiv.style.width = "110px";
	    		
	    		let authInputWarp = emailAuthInput.closest('.input-wrap');
	    		
	    		let emailAuthWrap = document.createElement('div');
	    		emailAuthWrap.className = 'input-wrap d-flex align-items-center';
	    		
	    		let authWarnDiv = document.createElement('div');
	    		authWarnDiv.className = 'warn-div auth';
	    		authWarnDiv.textContent = '인증번호를 입력해주세요.';
	    		
	    		emailAuthWrap.appendChild(spaceDiv);
	    		emailAuthWrap.appendChild(authWarnDiv);
	    		
	    		authInputWarp.insertAdjacentElement('afterend', emailAuthWrap);
	            return;
	        }

	        fetch("/member/emailAuthCodeConfirm", {
	            method: "POST",
	            headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ emailAuthCode : emailAuthCode })
	        })
	        .then(response => response.json())
	        .then(data => {
	        	
	        	let existingWarnDiv = document.querySelector('.warn-div.auth');
                if (existingWarnDiv) {
                	existingWarnDiv.closest('.input-wrap').remove();
                }
	        	
	        	let spaceDiv = document.createElement('div');
	            spaceDiv.style.width = "110px";
	            
	            let authInputWarp = emailAuthInput.closest('.input-wrap');
	            
	            let emailAuthWrap = document.createElement('div');
	            emailAuthWrap.className = 'input-wrap d-flex align-items-center';
	            
	            let authWarnDiv = document.createElement('div');
	            authWarnDiv.className = 'warn-div auth';
	            authWarnDiv.textContent = data.msg;

	            if (data.result === true) {
	            	emailAuthInput.readOnly  = true;
	            	emailAuthInput.style.backgroundColor = '#f0f0f0';
	            	authBtn.disabled = true;
	            	authBtn.style.backgroundColor = '#f0f0f0';
	            	memberEmail.readOnly  = true;
	            	memberEmail.style.backgroundColor = '#f0f0f0';
	            	
	            	emailAuthCheck = true;
	            	
	                timerDiv.remove();
	                
	                authWarnDiv.style.color = "green";
	                
		    		emailAuthWrap.appendChild(spaceDiv);
		    		emailAuthWrap.appendChild(authWarnDiv);
		    		
		    		authInputWarp.insertAdjacentElement('afterend', emailAuthWrap);
	                
	            } else {
		    		
	            	emailAuthInput.focus();
	            	
		    		emailAuthWrap.appendChild(spaceDiv);
		    		emailAuthWrap.appendChild(authWarnDiv);
		    		
		    		authInputWarp.insertAdjacentElement('afterend', emailAuthWrap);
	            } 
	        })
	        .catch(error => {
	            console.error("메일 인증 오류 발생:", error);
	        });
	    });
	    
	    authWrap.appendChild(authLabel);
	    authWrap.appendChild(authInput);
	    authWrap.appendChild(authBtn);
	    
		timerWrap.appendChild(spaceDiv);
		timerWrap.appendChild(timerDiv);

	    let emailInputWrap = document.querySelector('.email-input-wrap'); 
	    
	    if (emailInputWrap) {
			newDiv.appendChild(authWrap);
			newDiv.appendChild(timerWrap);
			
			emailInputWrap.insertAdjacentElement('afterend', newDiv);
	    	startTimer();
	    }
	})
	.catch(error => {
		console.error("이메일 인증번호 전송 오류:", error);
	});
}