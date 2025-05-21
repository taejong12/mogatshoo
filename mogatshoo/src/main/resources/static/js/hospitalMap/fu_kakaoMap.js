// DOM 요소가 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
	console.log("DOM 로드 완료");
	// 안전장치 - 30초 후에 강제로 로딩 숨기기
	setTimeout(function() {
		if (document.getElementById('loading').style.display !== 'none') {
			console.log("안전장치 작동: 로딩 시간 초과");
			showLoading(false);
			showMessage("로딩 시간이 초과되었습니다. 페이지를 새로고침하거나 다시 시도해 주세요.", "error");
		}
	}, 30000);

	// 카카오맵 API 로드
	kakao.maps.load(function() {
		console.log("카카오맵 API 로드 완료");
		initMap();
		// 초기화 후 약간의 지연을 두고 위치 검색 시작
		setTimeout(function() {
			try {
				getCurrentLocation();
			} catch (e) {
				console.error("위치 검색 오류:", e);
				showLoading(false);
				showMessage("위치 검색 중 오류가 발생했습니다. 페이지를 새로고침해 주세요.", "error");
			}
		}, 1000);
	});
});



