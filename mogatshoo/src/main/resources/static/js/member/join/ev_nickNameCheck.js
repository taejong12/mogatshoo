document.addEventListener('DOMContentLoaded', function() {
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameWarnMsg = document.getElementById('nickNameWarnMsg');

	if(nickNameInput){
		
		nickNameWarnMsg.textContent = '';
		
		nickNameInput.addEventListener('input', function() {
			
			let nickNameVal = nickNameInput.value.trim();
			
			if (nickNameVal === '' || nickNameVal.length == 0) {
				nickNameWarnMsg.textContent = '';
				nickNameWarnMsg.style.color = "rgb(255, 107, 107)";
				nickNameCheck = false;
				return;
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
					nickNameWarnMsg.textContent = "사용 가능한 닉네임입니다.";
					nickNameWarnMsg.style.color = "rgb(129, 199, 132)";
					nickNameCheck = true;
				} else {
					nickNameWarnMsg.textContent = "이미 사용 중인 닉네임입니다.";
					nickNameWarnMsg.style.color = "rgb(255, 107, 107)";
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