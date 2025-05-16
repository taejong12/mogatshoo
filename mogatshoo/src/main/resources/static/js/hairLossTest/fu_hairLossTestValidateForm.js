// 폼 유효성 검사 함수 확인
			function validateForm() {
				console.log("validateForm 함수 호출됨");
				const idInput = document.getElementById('idInput');
				const imageInput = document.getElementById('imageInput');
				const predictionJson = document.getElementById('predictionJson');

				// 닉네임 검사
				if (!idInput.value.trim()) {
					alert('닉네임을 입력해주세요.');
					idInput.focus();
					return false;
				}

				// 이미지 검사
				if (!imageInput.files || imageInput.files.length === 0) {
					alert('이미지를 업로드해주세요.');
					return false;
				}

				// 예측 결과 검사
				if (!predictionJson.value) {
					alert('먼저 탈모 단계 분석을 완료해주세요.');
					return false;
				}

				return true;
			}