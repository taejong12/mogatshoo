// 현재 위치 가져오기
function getCurrentLocation() {
	showLoading(true);
	try {
		clearMessages();
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}

	// 위치 가져오기 타임아웃 설정 (15초)
	var locationTimeout = setTimeout(function() {
		console.log("위치 정보 가져오기 타임아웃");
		showMessage("위치 정보를 가져오는데 시간이 오래 걸립니다. 기본 위치(서울시청)에서 검색합니다.", "error");
		// 기본 위치에서 검색
		fallbackToDefaultLocation();
	}, 15000);

	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			function(position) {
				clearTimeout(locationTimeout); // 타임아웃 제거
				console.log("현재 위치 가져오기 성공");
				var lat = position.coords.latitude;
				var lng = position.coords.longitude;
				currentPosition = new kakao.maps.LatLng(lat, lng);

				// 지도 중심 이동
				map.setCenter(currentPosition);

				// 현재 위치 마커 표시
				var marker = new kakao.maps.Marker({
					map: map,
					position: currentPosition,
					image: new kakao.maps.MarkerImage(
						'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png',
						new kakao.maps.Size(24, 35)
					)
				});

				// 주변 병원 검색 (키워드 배열로 여러번 시도)
				searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);
			},
			function(error) {
				clearTimeout(locationTimeout); // 타임아웃 제거
				console.error("위치 가져오기 오류:", error);
				showMessage("위치 정보를 가져오는데 실패했습니다. 기본 위치(서울시청)에서 검색합니다.", "error");
				// 기본 위치에서 검색
				fallbackToDefaultLocation();
			},
			{
				enableHighAccuracy: true,
				timeout: 10000,
				maximumAge: 0
			}
		);
	} else {
		showMessage("이 브라우저에서는 위치 정보를 지원하지 않습니다. 기본 위치(서울시청)에서 검색합니다.", "error");
		// 기본 위치에서 검색
		fallbackToDefaultLocation();
	}
}
