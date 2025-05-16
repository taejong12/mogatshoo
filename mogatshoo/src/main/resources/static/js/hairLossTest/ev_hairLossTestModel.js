// 페이지 로드 시 모달 초기화 추가
			window.addEventListener('DOMContentLoaded', () => {
				// 페이지 로드 시에는 단순히 UI 초기화만 수행
				console.log("페이지가 로드되었습니다. 이미지를 업로드하고 분석 버튼을 클릭하세요.");

				try {
					document.getElementById('model-status').textContent = '이미지를 업로드하고 분석 버튼을 클릭하세요.';

					// 모달 초기화 (Bootstrap 5)
					var resultModalElement = document.getElementById('resultModal');
					if (resultModalElement) {
						console.log("모달 요소를 찾았습니다. 초기화를 시도합니다.");
						window.resultModal = new bootstrap.Modal(resultModalElement);
					} else {
						console.error("모달 요소를 찾을 수 없습니다.");
					}
				} catch (error) {
					console.error("초기화 오류:", error);
				}
			});