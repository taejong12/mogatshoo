// 아이디 중복 체크 이벤트
document.addEventListener('DOMContentLoaded', function() {
	
	let memberId = document.getElementById('memberId');
	let idWarnMsg = document.getElementById('idWarnMsg');
	
	if(memberId){
		
		idWarnMsg.textContent = '';
		
		memberId.addEventListener('input', function() {
			
			let memberIdVal = memberId.value.trim();
			
			if (memberIdVal === '' || memberIdVal.length == 0) {
				idWarnMsg.textContent = '';
				idWarnMsg.style.color = "rgb(255, 107, 107)";
				idCheck = false;
				return;
			}
			
			fetch("/member/idCheck", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({ memberId: memberIdVal })
			})
			.then(response => {
				if (!response.ok) {
			        throw new Error(`서버 오류: ${response.status} ${response.statusText}`);
			    }
				return response.json();
			})
			.then(data => {
				if (data.memberIdCheck) {
					idWarnMsg.textContent = "사용 가능한 아이디입니다.";
					idWarnMsg.style.color = "rgb(129, 199, 132)";
					idCheck = true;
				} else {
					idWarnMsg.textContent = "이미 사용 중인 아이디입니다.";
					idWarnMsg.style.color = "rgb(255, 107, 107)";
					idCheck = false;
				}
			})
			.catch(error => {
				console.error("아이디 중복 확인 오류:", error);
			});
		});
	}
});