function previewImage() {
				console.log("previewImage 함수 호출됨");
				const input = document.getElementById('imageInput');
				const preview = document.getElementById('image-preview');
				const analyzeButton = document.getElementById('analyzeButton');
				const predictionContainer = document.getElementById('prediction-container');

				if (input.files && input.files[0]) {
					console.log("파일이 선택됨:", input.files[0].name);

					const reader = new FileReader();

					reader.onload = function (e) {
						console.log("파일 읽기 완료");
						preview.src = e.target.result;
						preview.style.display = 'block';
						analyzeButton.style.display = 'inline-block';
						predictionContainer.style.display = 'none'; // 결과 숨기기
						document.getElementById('model-status').textContent = '이미지 분석 준비 완료. 분석 버튼을 클릭하세요.';
					};

					reader.onerror = function (e) {
						console.error("파일 읽기 오류:", e);
					};

					reader.readAsDataURL(input.files[0]);
				} else {
					console.log("선택된 파일 없음");
				}
			}