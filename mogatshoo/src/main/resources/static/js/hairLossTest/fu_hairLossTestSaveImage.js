// saveResult 함수 수정
function saveResult() {
	console.log("saveResult 함수 호출됨");

	// 유효성 검사
	if (!validateForm()) {
		return;
	}

	// 폼 데이터 준비
	const form = document.getElementById('boardForm');
	const formData = new FormData(form);

	// 로딩 표시
	document.getElementById('submitButton').disabled = true;
	document.getElementById('submitButton').innerText = '저장 중...';

	// 테스트: 모달 직접 표시 (API 호출 전)
	try {
		document.getElementById('modalMessage').innerHTML = `
                     <div class="text-center">
                         <i class="bi bi-info-circle" style="font-size: 3rem; color: var(--accent-color);"></i>
                         <h4 style="color: var(--accent-color); margin-top: 10px;">처리 중...</h4>
                         <p>데이터를 저장하는 중입니다.</p>
                     </div>
                 `;

		if (window.resultModal) {
			window.resultModal.show();
			console.log("모달 표시 시도 (window.resultModal 사용)");
		} else if (bootstrap && bootstrap.Modal) {
			const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));
			resultModal.show();
			console.log("모달 표시 시도 (new bootstrap.Modal 사용)");
		} else {
			console.error("bootstrap.Modal을 찾을 수 없음");
			// 대체 방법: 직접 표시
			const modalEl = document.getElementById('resultModal');
			modalEl.classList.add('show');
			modalEl.style.display = 'block';
			console.log("모달 표시 시도 (직접 DOM 조작)");
		}
	} catch (error) {
		console.error("모달 표시 오류:", error);
		alert("저장 처리 중입니다.");
	}

	// AJAX 요청
	console.log("AJAX 요청 시작");
	fetch('/hairLossTest/api', {
		method: 'POST',
		body: formData
	})
		.then(response => {
			console.log("서버 응답 받음:", response);
			if (!response.ok) {
				throw new Error('서버 응답 오류: ' + response.status);
			}
			return response.json();
		})
		// saveResult 함수의 성공 처리 부분 수정 (약 라인 380 근처)
		.then(data => {
			console.log('저장 성공:', data);

			// 모달에 성공 메시지 표시
			document.getElementById('modalMessage').innerHTML = `
                       <div class="text-center">
                           <i class="bi bi-check-circle" style="font-size: 3rem; color: var(--accent-color);"></i>
                           <h4 style="color: var(--accent-color); margin-top: 10px;">저장 성공!</h4>
                           <p>닉네임 <strong>${data.memberId}</strong>의 탈모 테스트 결과가 성공적으로 저장되었습니다.</p>
                           <p>탈모 단계: <strong>${data.hairStage}</strong></p>
                           <p style="margin-top: 20px;">잠시 후 메인 페이지로 이동합니다...</p>
                       </div>
                   `;

			// 모달 표시 시도 (이미 표시되어 있을 수 있음)
			try {
				if (window.resultModal) {
					// 이미 표시된 경우에는 아무 동작 안함
				} else {
					const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));
					resultModal.show();
				}

				// 3초 후 메인 페이지로 리다이렉트
				setTimeout(() => {
					window.location.href = '/';  // 메인 페이지로 리다이렉트
				}, 3000);

			} catch (error) {
				console.error("성공 모달 표시 오류:", error);
				// 오류 발생 시 즉시 리다이렉트
				window.location.href = '/';
			}

			// 버튼 상태 복원
			document.getElementById('submitButton').disabled = false;
			document.getElementById('submitButton').innerText = '결과 저장하기';
		})



		.catch(error => {
			console.error('저장 오류:', error);

			// 모달에 오류 메시지 표시
			document.getElementById('modalMessage').innerHTML = `
                     <div class="text-center">
                         <i class="bi bi-exclamation-triangle" style="font-size: 3rem; color: #EE5A5A;"></i>
                         <h4 style="color: #EE5A5A; margin-top: 10px;">저장 실패</h4>
                         <p>탈모 테스트 결과를 저장하는 중 오류가 발생했습니다.</p>
                         <p><small>${error.message}</small></p>
                     </div>
                 `;

			// 모달 표시 시도
			try {
				if (window.resultModal) {
					// 이미 표시되어 있을 수 있음
				} else {
					const resultModal = new bootstrap.Modal(document.getElementById('resultModal'));
					resultModal.show();
				}
			} catch (error) {
				console.error("오류 모달 표시 오류:", error);
			}

			// 버튼 상태 복원
			document.getElementById('submitButton').disabled = false;
			document.getElementById('submitButton').innerText = '결과 저장하기';
		});
}
