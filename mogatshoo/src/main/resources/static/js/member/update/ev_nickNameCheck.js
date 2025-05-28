document.addEventListener('DOMContentLoaded', function() {
	
	let nickNameInput = document.getElementById('memberNickName');
	let nickNameWarnMsg = document.getElementById('nickNameWarnMsg');

	if(nickNameInput){
		nickNameInput.addEventListener('input', function() {
			
			let nickNameVal = nickNameInput.value.trim();
			
			if (nickNameVal === '' || nickNameVal.length == 0) {
				nickNameWarnMsg.textContent = '';
				nickNameWarnMsg.style.display = 'none';
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
			.then(response => {
				if (!response.ok) {
			        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
			    }
				return response.json();
			})
			.then(data => {
				if (data.memberNickNameCheck) {
					nickNameWarnMsg.textContent = "사용 가능한 닉네임입니다.";
					nickNameWarnMsg.style.color = "green";
					nickNameWarnMsg.style.display = 'inline';
					nickNameCheck = true;
				} else {
					nickNameWarnMsg.textContent = "이미 사용 중인 닉네임입니다.";
					nickNameWarnMsg.style.color = "red";
					nickNameWarnMsg.style.display = 'inline';
					nickNameCheck = false;
				}
			})
			.catch(error => {
				console.error("닉네임 중복 확인 오류:", error);
			});
		});
	}
});