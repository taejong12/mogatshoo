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

				
			}