function fu_fortuneStart(){
	
	fetch("/fortune/start", {
		method: "POST"
	})
	.then(response => response.json())
	.then(map => {
		
		let fortuneItem = document.getElementById('fortuneItem');
		
		let fortuneItemDiv = document.createElement('div');
		fortuneItemDiv.textContent = map.fortuneItem;
		fortuneItemDiv.classList.add('mb-3');
		
		fortuneItem.appendChild(fortuneItemDiv);
		
		let fortuneMsg = document.getElementById('fortuneMsg');
		
		let fortuneMsgDiv = document.createElement('div');
		fortuneMsgDiv.textContent = map.fortuneMsg;
		fortuneMsgDiv.classList.add('mb-3');
		
		fortuneMsg.appendChild(fortuneMsgDiv);
		
		let fortuneBtn = document.getElementById('fortuneBtn');
		
		if (fortuneBtn) {
			fortuneBtn.remove();
		}
		
		let btnDiv = document.getElementById('btnDiv');
		
		//탈모진단하기
		let hairLossBtn = document.createElement('button');
		hairLossBtn.classList.add('btn', 'btn-warning', 'btn-lg');
		hairLossBtn.innerText = '탈모 진단하기';
		
		btnDiv.appendChild(hairLossBtn);
		
		hairLossBtn.addEventListener('click', function(){
			document.location.href="/hairLossTest/testHair";
		})
		
		let point = document.getElementById('point');
		point.textContent = map.point;
		
	})
	.catch(error => {
		console.error("운세 확인 오류:", error);
	});
}