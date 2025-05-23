// 아이디 중복 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	let memberId = document.getElementById('memberId');

	if(memberId){
		memberId.addEventListener('input', function() {
			
			let memberIdVal = memberId.value.trim();
			let idWarn = document.querySelector('.warn-div.id');
			
			if (memberIdVal === '' || memberIdVal.length == 0) {
				idCheckMessage.textContent = '';
				idCheckMessage.style.display = 'none';
				idCheck = false;
				return;
			}
			
			if(idWarn){
				idWarn.closest('.input-wrap').remove();
			}
			
			fetch("/member/idCheck", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ memberId: memberIdVal })
			})
			.then(response => response.json())
			.then(data => {
				if (data.memberIdCheck) {
					idCheckMessage.textContent = "사용 가능한 아이디입니다.";
					idCheckMessage.style.color = "green";
					idCheckMessage.style.display = 'inline';
					idCheck = true;
				} else {
					idCheckMessage.textContent = "이미 사용 중인 아이디입니다.";
					idCheckMessage.style.color = "red";
					idCheckMessage.style.display = 'inline';
					idCheck = false;
				}
			})
			.catch(error => {
				console.error("아이디 중복 확인 오류:", error);
				window.location.href = "/error/globalError";
			});
		});
	}
});