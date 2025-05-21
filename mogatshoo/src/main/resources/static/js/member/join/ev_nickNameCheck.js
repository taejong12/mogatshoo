document.addEventListener('DOMContentLoaded', function() {
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameCheckMessage = document.getElementById('nickNameCheckMessage');

	if(nickNameInput){
		nickNameInput.addEventListener('input', function() {
			
			let nickNameVal = nickNameInput.value.trim();
			let nickNameWarn = document.querySelector('.warn-div.nickName');
			
			if (nickNameVal === '' || nickNameVal.length == 0) {
				nickNameCheckMessage.textContent = '';
				nickNameCheckMessage.style.display = 'none';
				nickNameCheck = false;
				return;
			}
			
			if(nickNameWarn){
				nickNameWarn.closest('.input-wrap').remove();
			}
			
			fetch("/member/nickNameCheck", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ memberNickName: nickNameVal })
			})
			.then(response => response.json())
			.then(data => {
				if (data.memberNickNameCheck) {
					nickNameCheckMessage.textContent = "사용 가능한 닉네임입니다.";
					nickNameCheckMessage.style.color = "green";
					nickNameCheckMessage.style.display = 'inline';
					nickNameCheck = true;
				} else {
					nickNameCheckMessage.textContent = "이미 사용 중인 닉네임입니다.";
					nickNameCheckMessage.style.color = "red";
					nickNameCheckMessage.style.display = 'inline';
					nickNameCheck = false;
				}
			})
			.catch(error => {
				console.error("닉네임 중복 확인 오류:", error);
				window.location.href = "/error/globalError";
			});
		});
	}
});