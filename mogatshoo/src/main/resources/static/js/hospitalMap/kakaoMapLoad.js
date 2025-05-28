// 전역 변수
var map;
var currentPosition = null;
var markers = [];
var infowindows = [];
var searchTimeout;
var isFirstSearch = true;
var userLat, userLng; // 사용자 위치를 저장할 변수
var radiusCircle = null; // 검색 반경 원
var locationOverlay = null; // 내 위치 오버레이

// DOM 요소가 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
	console.log("DOM 로드 완료");

	// 로딩 표시
function showLoading(show) {
	try {
		var loadingElement = document.getElementById('loading');
		if (loadingElement) {
			loadingElement.style.display = show ? 'flex' : 'none';
		} else {
			console.warn("로딩 영역(#loading)을 찾을 수 없습니다");
		}
	} catch (e) {
		console.error("로딩 표시 오류:", e);
	}
}

// 키워드로 검색
function searchByKeyword() {
	var keyword = document.getElementById('keyword').value;
	if (!keyword.trim()) {
		showMessage("검색어를 입력해주세요.", "error");
		return;
	}

	showLoading(true);
	try {
		clearMessages();
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}
	removeAllMarkers();

	// 현재 위치 마커 다시 추가 (있는 경우)
	if (currentPosition) {
		var markerImage = new kakao.maps.MarkerImage(
			'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/red_b.png',
			new kakao.maps.Size(50, 45),
			{ offset: new kakao.maps.Point(15, 43) }
		);

		var marker = new kakao.maps.Marker({
			map: map,
			position: currentPosition,
			image: markerImage,
			zIndex: 10
		});

		markers.push(marker);

		// 오버레이 다시 표시
		if (locationOverlay) {
			locationOverlay.setMap(map);
		}

		// 검색 반경 다시 표시
		if (radiusCircle) {
			radiusCircle.setMap(map);
		}
	}

	// 검색 타임아웃 설정 (10초)
	if (searchTimeout) {
		clearTimeout(searchTimeout);
	}
	searchTimeout = setTimeout(function() {
		console.log("검색 타임아웃 발생");
		showMessage("검색 시간이 초과되었습니다. 다시 시도해 주세요.", "error");
		showLoading(false);
	}, 10000);

	var places = new kakao.maps.services.Places();
	places.keywordSearch(keyword, function(result, status) {
		clearTimeout(searchTimeout);

		if (status === kakao.maps.services.Status.OK && result.length > 0) {
			displaySearchResults(result);
			showMessage(result.length + "개의 검색 결과를 찾았습니다.", "info");
		} else {
			showMessage("검색 결과가 없습니다. 다른 키워드로 검색해보세요.", "error");
			if (document.getElementById('hospital-list')) {
				document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
			}
		}
		showLoading(false);
	});
}

// 카테고리로 검색 (수정된 버전)
function searchByCategory(category) {
	showLoading(true);
	try {
		clearMessages();
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}

	var position = currentPosition || map.getCenter();
	var radius = parseInt(document.getElementById('radius').value, 10);
	console.log("카테고리 검색:", category, "반경:", radius, "미터");

	removeAllMarkers();

	// 현재 위치 마커 다시 추가 (있는 경우)
	if (currentPosition) {
		var markerImage = new kakao.maps.MarkerImage(
			'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/red_b.png',
			new kakao.maps.Size(50, 45),
			{ offset: new kakao.maps.Point(15, 43) }
		);

		var marker = new kakao.maps.Marker({
			map: map,
			position: currentPosition,
			image: markerImage,
			zIndex: 10
		});

		markers.push(marker);

		// 오버레이 다시 표시
		if (locationOverlay) {
			locationOverlay.setMap(map);
		}

		// 검색 반경 다시 표시
		if (radiusCircle) {
			radiusCircle.setMap(map);
		}
	}

	// 검색 타임아웃 설정 (10초)
	if (searchTimeout) {
		clearTimeout(searchTimeout);
	}
	searchTimeout = setTimeout(function() {
		console.log("카테고리 검색 타임아웃 발생");
		showMessage("검색 시간이 초과되었습니다. 다시 시도해 주세요.", "error");
		showLoading(false);
	}, 10000);

	var places = new kakao.maps.services.Places();
	places.keywordSearch(category, function(result, status) {
		clearTimeout(searchTimeout);

		if (status === kakao.maps.services.Status.OK && result.length > 0) {
			// ✅ 중복 필터링 제거 - 카카오맵 API가 이미 반경으로 필터링했으므로 바로 사용
			console.log("API 필터링된 결과 수:", result.length);
			
			displaySearchResults(result);
			showMessage(result.length + "개의 " + category + " 검색 결과를 찾았습니다.", "info");
		} else {
			showMessage("검색 결과가 없습니다. 다른 키워드로 검색해보세요.", "error");
			if (document.getElementById('hospital-list')) {
				document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
			}
		}
		showLoading(false);
	}, {
		location: position,
		radius: radius,
		sort: kakao.maps.services.SortBy.DISTANCE
	});
}

// 현재 위치 가져오기
function getCurrentLocation() {
	showLoading(true);
	try {
		clearMessages();
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}

	showMessage("정확한 위치 정보를 가져오는 중입니다...", "info");

	// 위치 가져오기 타임아웃 설정 (15초)
	var locationTimeout = setTimeout(function() {
		console.log("위치 정보 가져오기 타임아웃");
		showMessage("위치 정보를 가져오는데 시간이 오래 걸립니다. 다시 시도하거나 직접 검색해 주세요.", "error");
		showLoading(false);
	}, 15000);

	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			function(position) {
				clearTimeout(locationTimeout); // 타임아웃 제거

				// 위도와 경도 저장
				userLat = position.coords.latitude;
				userLng = position.coords.longitude;

				console.log("현재 위치 가져오기 성공:", userLat, userLng);
				console.log("위치 정확도:", position.coords.accuracy, "미터");

				// 모든 마커 제거
				removeAllMarkers();

				// 카카오맵 API를 사용하여 위치 표시
				kakao.maps.load(function() {
					// 현재 위치 객체 생성
					currentPosition = new kakao.maps.LatLng(userLat, userLng);

					// 지도 중심 이동
					map.setCenter(currentPosition);
					map.setLevel(4); // 적절한 줌 레벨로 설정

					// 현재 위치 마커 표시 (빨간색으로 변경)
					var markerImage = new kakao.maps.MarkerImage(
						// 빨간색 마커 이미지 URL
						'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/red_b.png',
						new kakao.maps.Size(50, 45),
						{ offset: new kakao.maps.Point(15, 43) }
					);

					var marker = new kakao.maps.Marker({
						map: map,
						position: currentPosition,
						image: markerImage,
						zIndex: 10
					});

					markers.push(marker);

					// 이전 오버레이 제거
					if (locationOverlay) {
						locationOverlay.setMap(null);
					}

					// 검색 반경 시각화
					updateSearchRadius();

					// 주변 병원 검색
					searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);

					showMessage("현재 위치를 찾았습니다. 반경 " + (document.getElementById('radius').value / 1000) + "km 이내 탈모 관련 병원을 검색합니다.", "info");
				});
			},
			function(error) {
				clearTimeout(locationTimeout); // 타임아웃 제거
				console.error("위치 가져오기 오류:", error);

				var errorMsg = "위치 정보를 가져오는데 실패했습니다.";
				switch (error.code) {
					case error.PERMISSION_DENIED:
						errorMsg = "위치 정보 접근 권한이 거부되었습니다. 브라우저 설정에서 위치 정보 접근을 허용해 주세요.";
						break;
					case error.POSITION_UNAVAILABLE:
						errorMsg = "위치 정보를 사용할 수 없습니다. 네트워크 연결을 확인해 주세요.";
						break;
					case error.TIMEOUT:
						errorMsg = "위치 정보 요청 시간이 초과되었습니다. 다시 시도해 주세요.";
						break;
					case error.UNKNOWN_ERROR:
						errorMsg = "알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.";
						break;
				}

				showMessage(errorMsg, "error");
				showLoading(false);
			},
			{
				enableHighAccuracy: true, // 높은 정확도 요청
				timeout: 10000,
				maximumAge: 0 // 캐시된 위치 사용 안 함
			}
		);
	} else {
		clearTimeout(locationTimeout);
		showMessage("이 브라우저에서는 위치 정보를 지원하지 않습니다. 검색창에서 직접 검색해 주세요.", "error");
		showLoading(false);
	}
}

// 위치 정보 테스트 함수 (개발자 도구 콘솔에서 호출 가능)
function testLocation() {
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			function(position) {
				console.log("브라우저 위치 API 테스트:");
				console.log("위도:", position.coords.latitude);
				console.log("경도:", position.coords.longitude);
				console.log("정확도:", position.coords.accuracy, "미터");
				console.log("고도:", position.coords.altitude);
				console.log("고도 정확도:", position.coords.altitudeAccuracy);
				console.log("방향:", position.coords.heading);
				console.log("속도:", position.coords.speed);
				console.log("타임스탬프:", new Date(position.timestamp));
			},
			function(error) {
				console.error("위치 정보 가져오기 오류:", error.code, error.message);
			},
			{
				enableHighAccuracy: true,
				timeout: 15000,
				maximumAge: 0
			}
		);
	} else {
		console.error("이 브라우저는 위치 정보를 지원하지 않습니다.");
	}
}

// 반경별 검색 결과 비교 함수 (디버깅용)
function compareRadiusResults() {
	if (!currentPosition) {
		console.log("❌ 현재 위치가 설정되지 않았습니다.");
		return;
	}

	const radiuses = [1000, 2000, 5000, 10000]; // 1km, 2km, 5km, 10km
	const keyword = '탈모 병원';
	const results = {};
	
	console.log("🔍 반경별 검색 결과 비교 시작...");
	console.log("📍 현재 위치:", currentPosition.getLat(), currentPosition.getLng());
	console.log("🔎 검색 키워드:", keyword);
	console.log("=".repeat(50));
	
	let completedSearches = 0;
	
	radiuses.forEach(radius => {
		const places = new kakao.maps.services.Places();
		
		places.keywordSearch(keyword, function(result, status) {
			completedSearches++;
			
			if (status === kakao.maps.services.Status.OK) {
				results[radius] = result.map(place => ({
					name: place.place_name,
					address: place.address_name,
					distance: Math.round(getDistance(currentPosition, new kakao.maps.LatLng(place.y, place.x))),
					id: place.id
				}));
				
				console.log(`📏 ${radius/1000}km 반경 결과 (${result.length}개):`);
				results[radius].forEach((place, index) => {
					console.log(`  ${index+1}. ${place.name} - ${place.distance}m`);
				});
				console.log("");
				
			} else {
				results[radius] = [];
				console.log(`📏 ${radius/1000}km 반경: 검색 결과 없음`);
			}
			
			// 모든 검색이 완료되면 비교 분석
			if (completedSearches === radiuses.length) {
				analyzeRadiusResults(results);
			}
		}, {
			location: currentPosition,
			radius: radius,
			sort: kakao.maps.services.SortBy.DISTANCE
		});
	});
}

function analyzeRadiusResults(results) {
	console.log("📊 결과 분석:");
	console.log("=".repeat(50));
	
	// 각 반경별 병원 ID 수집
	const hospitalSets = {};
	Object.keys(results).forEach(radius => {
		hospitalSets[radius] = new Set(results[radius].map(place => place.id));
	});
	
	// 1km에 있는 병원이 더 큰 반경에서도 나타나는지 확인
	const radius1km = hospitalSets['1000'];
	const radius2km = hospitalSets['2000'];
	const radius5km = hospitalSets['5000'];
	const radius10km = hospitalSets['10000'];
	
	console.log("🔍 포함 관계 분석:");
	
	if (radius1km.size > 0) {
		const in2km = [...radius1km].filter(id => radius2km.has(id)).length;
		const in5km = [...radius1km].filter(id => radius5km.has(id)).length;
		const in10km = [...radius1km].filter(id => radius10km.has(id)).length;
		
		console.log(`1km 결과 중 2km에도 포함: ${in2km}/${radius1km.size} (${Math.round(in2km/radius1km.size*100)}%)`);
		console.log(`1km 결과 중 5km에도 포함: ${in5km}/${radius1km.size} (${Math.round(in5km/radius1km.size*100)}%)`);
		console.log(`1km 결과 중 10km에도 포함: ${in10km}/${radius1km.size} (${Math.round(in10km/radius1km.size*100)}%)`);
	}
	
	if (radius2km.size > 0) {
		const in5km = [...radius2km].filter(id => radius5km.has(id)).length;
		const in10km = [...radius2km].filter(id => radius10km.has(id)).length;
		
		console.log(`2km 결과 중 5km에도 포함: ${in5km}/${radius2km.size} (${Math.round(in5km/radius2km.size*100)}%)`);
		console.log(`2km 결과 중 10km에도 포함: ${in10km}/${radius2km.size} (${Math.round(in10km/radius2km.size*100)}%)`);
	}
	
	console.log("");
	console.log("🆕 각 반경에서만 나타나는 새로운 병원:");
	
	// 각 반경에서 새로 추가되는 병원들
	const only2km = [...radius2km].filter(id => !radius1km.has(id));
	const only5km = [...radius5km].filter(id => !radius2km.has(id));
	const only10km = [...radius10km].filter(id => !radius5km.has(id));
	
	console.log(`2km에서 새로 추가: ${only2km.length}개`);
	console.log(`5km에서 새로 추가: ${only5km.length}개`);
	console.log(`10km에서 새로 추가: ${only10km.length}개`);
	
	// 실제 누락된 병원이 있는지 확인
	console.log("");
	console.log("⚠️ 예상과 다른 결과:");
	
	const missing1kmIn5km = [...radius1km].filter(id => !radius5km.has(id));
	const missing2kmIn5km = [...radius2km].filter(id => !radius5km.has(id));
	
	if (missing1kmIn5km.length > 0) {
		console.log(`❌ 1km 결과가 5km에서 누락된 병원: ${missing1kmIn5km.length}개`);
		missing1kmIn5km.forEach(id => {
			const hospital = results['1000'].find(p => p.id === id);
			if (hospital) {
				console.log(`   - ${hospital.name} (${hospital.distance}m)`);
			}
		});
	}
	
	if (missing2kmIn5km.length > 0) {
		console.log(`❌ 2km 결과가 5km에서 누락된 병원: ${missing2kmIn5km.length}개`);
		missing2kmIn5km.forEach(id => {
			const hospital = results['2000'].find(p => p.id === id);
			if (hospital) {
				console.log(`   - ${hospital.name} (${hospital.distance}m)`);
			}
		});
	}
}

// 콘솔에서 테스트 가능하도록 전역 함수로 노출
window.testLocation = testLocation;
window.compareRadiusResults = compareRadiusResults;
	if (document.getElementById('loading')) {
		showLoading(true);
	} else {
		console.warn("로딩 요소(#loading)를 찾을 수 없습니다");
	}

	// 안전장치 - 30초 후에 강제로 로딩 숨기기
	setTimeout(function() {
		if (document.getElementById('loading') && document.getElementById('loading').style.display !== 'none') {
			console.log("안전장치 작동: 로딩 시간 초과");
			showLoading(false);
			showMessage("로딩 시간이 초과되었습니다. 페이지를 새로고침하거나 다시 시도해 주세요.", "error");
		}
	}, 30000);

	// 반경 변경 이벤트 설정 추가
	var radiusSelect = document.getElementById('radius');
	if (radiusSelect) {
		radiusSelect.addEventListener('change', function() {
			if (map && currentPosition) {
				updateSearchRadius();
			}
		});
	}

	// 먼저 현재 위치를 가져온 다음 지도 초기화
	getLocationAndInitMap();
});

// 현재 위치를 가져온 다음 지도 초기화
function getLocationAndInitMap() {
	if (navigator.geolocation) {
		// 위치 가져오기 타임아웃 설정 (15초) 시간 줄이지 마셈 좀 오래걸려요
		var locationTimeout = setTimeout(function() {
			console.log("위치 정보 가져오기 타임아웃");
			showMessage("위치 정보를 가져오는데 시간이 오래 걸립니다. 기본 위치 없이 시작합니다.", "error");
			// 기본 위치 없이 지도 초기화
			loadKakaoMapWithoutPosition();
		}, 15000);

		navigator.geolocation.getCurrentPosition(
			function(position) {
				clearTimeout(locationTimeout); // 타임아웃 제거
				console.log("현재 위치 가져오기 성공:", position.coords.latitude, position.coords.longitude);
				console.log("위치 정확도:", position.coords.accuracy, "미터");

				// 위도와 경도 저장 (LatLng 객체는 아직 만들지 않음)
				userLat = position.coords.latitude;
				userLng = position.coords.longitude;

				// 카카오맵 API를 로드한 후 위치 사용
				loadKakaoMapWithPosition();
			},
			function(error) {
				clearTimeout(locationTimeout); // 타임아웃 제거
				console.error("위치 가져오기 오류:", error);

				var errorMsg = "위치 정보를 가져오는데 실패했습니다.";
				switch (error.code) {
					case error.PERMISSION_DENIED:
						errorMsg = "위치 정보 접근 권한이 거부되었습니다. 브라우저 설정에서 위치 정보 접근을 허용해 주세요.";
						break;
					case error.POSITION_UNAVAILABLE:
						errorMsg = "위치 정보를 사용할 수 없습니다. 네트워크 연결을 확인해 주세요.";
						break;
					case error.TIMEOUT:
						errorMsg = "위치 정보 요청 시간이 초과되었습니다. 다시 시도해 주세요.";
						break;
					case error.UNKNOWN_ERROR:
						errorMsg = "알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.";
						break;
				}

				showMessage(errorMsg, "error");
				// 기본 위치 없이 지도 초기화
				loadKakaoMapWithoutPosition();
			},
			{
				enableHighAccuracy: true, // 높은 정확도 요청
				timeout: 10000,
				maximumAge: 0 // 캐시된 위치 사용 안 함
			}
		);
	} else {
		showMessage("이 브라우저에서는 위치 정보를 지원하지 않습니다. 기본 위치 없이 시작합니다.", "error");
		// 기본 위치 없이 지도 초기화
		loadKakaoMapWithoutPosition();
	}
}

// 카카오맵 API 로드 후 현재 위치로 초기화
function loadKakaoMapWithPosition() {
	// 카카오맵 API가 전역 변수로 있는지 확인
	if (typeof kakao === 'undefined' || typeof kakao.maps === 'undefined') {
		console.error("카카오맵 API가 로드되지 않았습니다");
		showMessage("지도 서비스를 불러올 수 없습니다. 페이지를 새로고침해 주세요.", "error");
		showLoading(false);
		return;
	}

	// 카카오맵 API 로드
	kakao.maps.load(function() {
		console.log("카카오맵 API 로드 완료 (위치 있음)");

		// 이제 kakao.maps.LatLng 생성자를 안전하게 사용할 수 있음
		currentPosition = new kakao.maps.LatLng(userLat, userLng);

		var container = document.getElementById('map');
		if (!container) {
			console.error("지도 컨테이너(#map)를 찾을 수 없습니다");
			showMessage("지도를 표시할 수 없습니다. 페이지를 새로고침해 주세요.", "error");
			showLoading(false);
			return;
		}

		var options = {
			center: currentPosition,
			level: 4
		};

		map = new kakao.maps.Map(container, options);

		// 현재 위치 마커 표시 (빨간색으로 변경)
		var markerImage = new kakao.maps.MarkerImage(
			'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/red_b.png',
			new kakao.maps.Size(50, 45),
			{ offset: new kakao.maps.Point(15, 43) }
		);

		var marker = new kakao.maps.Marker({
			map: map,
			position: currentPosition,
			image: markerImage,
			zIndex: 10
		});

		markers.push(marker);

		// 반경 변경 이벤트 설정
		setupRadiusChangeEvent();

		// 초기 검색 반경 시각화
		updateSearchRadius();

		// 주변 병원 검색
		searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);
	});
}

// 카카오맵 API 로드 후 기본 위치 없이 초기화
function loadKakaoMapWithoutPosition() {
	// 카카오맵 API가 전역 변수로 있는지 확인
	if (typeof kakao === 'undefined' || typeof kakao.maps === 'undefined') {
		console.error("카카오맵 API가 로드되지 않았습니다");
		showMessage("지도 서비스를 불러올 수 없습니다. 페이지를 새로고침해 주세요.", "error");
		showLoading(false);
		return;
	}

	kakao.maps.load(function() {
		console.log("카카오맵 API 로드 완료 (위치 없음)");

		var container = document.getElementById('map');
		if (!container) {
			console.error("지도 컨테이너(#map)를 찾을 수 없습니다");
			showMessage("지도를 표시할 수 없습니다. 페이지를 새로고침해 주세요.", "error");
			showLoading(false);
			return;
		}

		var options = {
			center: new kakao.maps.LatLng(36.2, 127.9), // 대한민국 중심 좌표 (대략)
			level: 13 // 전국이 보이는 레벨
		};

		map = new kakao.maps.Map(container, options);
		showMessage("현재 위치를 사용할 수 없습니다. 검색창에서 지역을 직접 검색해주세요.", "info");
		showLoading(false);

		// 반경 변경 이벤트 설정
		setupRadiusChangeEvent();
	});
}

// 검색 반경 시각화 함수
function updateSearchRadius() {
	// 이전 원이 있으면 제거
	if (radiusCircle) {
		radiusCircle.setMap(null);
	}

	// 현재 위치가 없으면 반환
	if (!currentPosition) return;

	// 선택된 반경 가져오기 (미터 단위)
	var radius = parseInt(document.getElementById('radius').value, 10);

	// 반경 원 생성
	radiusCircle = new kakao.maps.Circle({
		center: currentPosition,
		radius: radius, // 사용자가 선택한 반경 (미터 단위)
		strokeWeight: 3,
		strokeColor: '#FF3333', // 진한 빨간색 테두리
		strokeOpacity: 0.8,
		strokeStyle: 'solid',
		fillColor: '#FF9999', // 연한 빨간색 채우기
		fillOpacity: 0.2
	});

	// 지도에 원 표시
	radiusCircle.setMap(map);

	// 원이 지도에 완전히 보이도록 지도 범위 조정
	var bounds = new kakao.maps.LatLngBounds();

	// 원의 북동쪽 좌표
	var ne = new kakao.maps.LatLng(
		currentPosition.getLat() + radius / (111000), // 대략적인 위도 1도당 111km
		currentPosition.getLng() + radius / (111000 * Math.cos(currentPosition.getLat() * Math.PI / 180))
	);

	// 원의 남서쪽 좌표
	var sw = new kakao.maps.LatLng(
		currentPosition.getLat() - radius / (111000),
		currentPosition.getLng() - radius / (111000 * Math.cos(currentPosition.getLat() * Math.PI / 180))
	);

	bounds.extend(ne);
	bounds.extend(sw);

	// 지도 범위 설정 (약간의 여유 공간 추가)
	map.setBounds(bounds, 50);

	console.log("검색 반경 업데이트:", radius, "미터");
}

// 반경 변경 시 이벤트 처리 (수정된 버전)
function setupRadiusChangeEvent() {
	var radiusSelect = document.getElementById('radius');
	if (radiusSelect) {
		radiusSelect.addEventListener('change', function() {
			console.log("검색 반경 변경:", this.value, "미터");

			// 검색 반경 시각화 업데이트
			updateSearchRadius();

			// 현재 위치가 있으면 재검색 수행
			if (currentPosition) {
				showLoading(true);
				removeAllMarkers();
				// 마커를 다시 추가
				var markerImage = new kakao.maps.MarkerImage(
					'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/red_b.png',
					new kakao.maps.Size(50, 45),
					{ offset: new kakao.maps.Point(15, 43) }
				);

				var marker = new kakao.maps.Marker({
					map: map,
					position: currentPosition,
					image: markerImage,
					zIndex: 10
				});

				markers.push(marker);

				// 오버레이 다시 표시
				if (locationOverlay) {
					locationOverlay.setMap(map);
				}

				// 수정된 함수 호출 - 키워드 배열 전달
				searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);
				showMessage("반경 " + (radiusSelect.value / 1000) + "km 이내로 재검색합니다.", "info");
			}
		});
		console.log("개선된 반경 변경 이벤트 설정 완료");
	}
}

// 수정된 검색 함수 - 모든 키워드를 동시에 검색하여 결과 합치기
function searchMultipleKeywords(inputKeywords) {
	// 매개변수가 전달되면 사용하고, 없으면 기본 키워드 사용
	const keywords = inputKeywords || ['탈모 병원', '두피 클리닉', '피부과', '모발이식'];
	const position = currentPosition || map.getCenter();
	const radius = parseInt(document.getElementById('radius').value, 10);
	
	console.log("🔍 모든 키워드 동시 검색 시작");
	console.log("📏 검색 반경:", radius, "미터");
	console.log("🎯 검색 키워드:", keywords);
	console.log("📥 입력받은 키워드:", inputKeywords); // 디버깅용
	
	let completedSearches = 0;
	let allResults = [];
	const seenPlaces = new Set(); // 중복 제거용
	
	// 검색 타임아웃 설정
	if (searchTimeout) {
		clearTimeout(searchTimeout);
	}
	searchTimeout = setTimeout(function() {
		console.log("통합 검색 타임아웃 발생");
		showMessage("검색 시간이 초과되었습니다. 다시 시도해 주세요.", "error");
		showLoading(false);
	}, 15000); // 여러 검색이므로 시간을 좀 더 길게
	
	keywords.forEach((keyword, index) => {
		const places = new kakao.maps.services.Places();
		
		places.keywordSearch(keyword, function(result, status) {
			completedSearches++;
			console.log(`📋 ${keyword} 검색 완료: ${result ? result.length : 0}개 결과`);
			
			if (status === kakao.maps.services.Status.OK && result && result.length > 0) {
				// 중복 제거하면서 결과 합치기
				result.forEach(place => {
					if (!seenPlaces.has(place.id)) {
						seenPlaces.add(place.id);
						allResults.push({
							...place,
							searchKeyword: keyword // 어떤 키워드로 찾았는지 기록
						});
					}
				});
			}
			
			// 모든 키워드 검색이 완료되면 결과 표시
			if (completedSearches === keywords.length) {
				clearTimeout(searchTimeout);
				
				if (allResults.length > 0) {
					// 거리순으로 정렬
					allResults.sort((a, b) => {
						const distanceA = getDistance(position, new kakao.maps.LatLng(a.y, a.x));
						const distanceB = getDistance(position, new kakao.maps.LatLng(b.y, b.x));
						return distanceA - distanceB;
					});
					
					console.log(`✅ 통합 검색 완료: 총 ${allResults.length}개 결과`);
					
					// 각 키워드별 결과 수 로그
					const keywordCounts = {};
					allResults.forEach(place => {
						keywordCounts[place.searchKeyword] = (keywordCounts[place.searchKeyword] || 0) + 1;
					});
					console.log("📊 키워드별 결과:", keywordCounts);
					
					displaySearchResults(allResults);
					if (isFirstSearch) {
						showMessage(`${allResults.length}개의 탈모 관련 병원을 찾았습니다.`, "info");
						isFirstSearch = false;
					}
				} else {
					console.log("❌ 모든 키워드 검색 결과 없음");
					showMessage("주변에 탈모 관련 병원을 찾을 수 없습니다. 검색 반경을 넓히거나 다른 키워드로 검색해보세요.", "error");
					if (document.getElementById('hospital-list')) {
						document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
					}
				}
				showLoading(false);
			}
		}, {
			location: position,
			radius: radius,
			sort: kakao.maps.services.SortBy.DISTANCE
		});
	});
}

// 검색 결과 표시 함수
function displaySearchResults(places) {
	try {
		var bounds = new kakao.maps.LatLngBounds();
		var hospitalList = document.getElementById('hospital-list');

		if (hospitalList) {
			hospitalList.innerHTML = '';
		} else {
			console.warn("hospital-list 요소를 찾을 수 없습니다");
		}

		console.log("표시할 장소 수:", places.length);

		// 현재 위치가 있으면 경계에 추가 (지도 범위 조정에 포함)
		if (currentPosition) {
			bounds.extend(currentPosition);
		}

		// 검색 결과용 별도 배열 생성 (현재 위치 마커와 분리)
		var searchResultMarkers = [];
		var searchResultInfowindows = [];

		for (var i = 0; i < places.length; i++) {
			var place = places[i];
			try {
				// 마커 생성
				var marker = new kakao.maps.Marker({
					map: map,
					position: new kakao.maps.LatLng(place.y, place.x)
				});

				// 전체 마커 배열에 추가 (기존 코드 호환성 유지)
				markers.push(marker);

				// 검색 결과 전용 배열에도 추가
				searchResultMarkers.push(marker);

				var placePosition = new kakao.maps.LatLng(place.y, place.x);
				bounds.extend(placePosition);

				// 현재 위치와의 거리 계산
				var distanceText = '';
				if (currentPosition) {
					var distance = getDistance(currentPosition, placePosition);
					if (distance >= 1000) {
						distanceText = '<div style="font-size:12px;color:#FF3333;margin-bottom:5px;">내 위치에서 ' + (distance / 1000).toFixed(1) + 'km</div>';
					} else {
						distanceText = '<div style="font-size:12px;color:#FF3333;margin-bottom:5px;">내 위치에서 ' + Math.round(distance) + 'm</div>';
					}
				}

				// 검색 키워드 표시 (새로 추가)
				var keywordText = '';
				if (place.searchKeyword) {
					keywordText = '<div style="font-size:11px;color:#007bff;margin-bottom:5px;">🔍 ' + place.searchKeyword + '</div>';
				}

				// 인포윈도우 내용 생성
				var infoContent = '<div style="padding:15px;min-width:250px;max-width:300px;">' +
					'<h5 style="margin-top:0;margin-bottom:8px;color:#333;font-weight:bold;">' + place.place_name + '</h5>' +
					keywordText +
					distanceText +
					'<div style="font-size:14px;color:#666;margin-bottom:5px;line-height:1.4;">' + place.address_name + '</div>';

				if (place.phone) {
					infoContent += '<div style="font-size:14px;color:#666;margin-bottom:8px;">📞 ' + place.phone + '</div>';
				}

				// 카테고리 정보가 있으면 표시
				if (place.category_name) {
					infoContent += '<div style="font-size:12px;color:#999;margin-bottom:8px;">🏷️ ' + place.category_name + '</div>';
				}

				if (place.place_url) {
					infoContent += '<div style="margin-top:10px;"><a href="' + place.place_url + '" target="_blank" style="color:#007bff;font-size:13px;text-decoration:none;padding:5px 10px;border:1px solid #007bff;border-radius:4px;display:inline-block;">📍 상세 정보 보기</a></div>';
				}

				infoContent += '</div>';

				var infowindow = new kakao.maps.InfoWindow({
					content: infoContent,
					removable: true // X 버튼으로 닫을 수 있게 함
				});

				// 전체 인포윈도우 배열에 추가 (기존 코드 호환성 유지)
				infowindows.push(infowindow);

				// 검색 결과 전용 배열에도 추가
				searchResultInfowindows.push(infowindow);

				// 마커 클릭 이벤트 - 클로저를 사용하여 각 마커마다 올바른 인포윈도우 연결
				(function(currentMarker, currentInfowindow, placeName) {
					kakao.maps.event.addListener(currentMarker, 'click', function() {
						console.log("마커 클릭됨:", placeName);
						closeAllInfowindows();
						currentInfowindow.open(map, currentMarker);
						console.log("인포윈도우 열림:", placeName);
					});
				})(marker, infowindow, place.place_name);

				// 병원 목록이 있을 경우만 항목 추가
				if (hospitalList) {
					// 병원 목록 아이템 생성
					var item = document.createElement('div');
					item.className = 'hospital-item';

					// 거리 텍스트 (HTML 태그 제거)
					var distanceTextPlain = '';
					if (currentPosition) {
						var distance = getDistance(currentPosition, placePosition);
						if (distance >= 1000) {
							distanceTextPlain = ' (약 ' + (distance / 1000).toFixed(1) + 'km)';
						} else {
							distanceTextPlain = ' (약 ' + Math.round(distance) + 'm)';
						}
					}

					// 검색 키워드 표시
					var keywordBadge = '';
					if (place.searchKeyword) {
						keywordBadge = '<span style="font-size:10px;background:#e3f2fd;color:#1976d2;padding:2px 6px;border-radius:10px;margin-left:5px;">' + place.searchKeyword + '</span>';
					}

					item.innerHTML =
						'<div class="hospital-name">' + place.place_name +
						'<span class="hospital-distance">' + distanceTextPlain + '</span>' +
						keywordBadge +
						'</div>' +
						'<div class="hospital-address">' + place.address_name + '</div>' +
						'<div class="hospital-phone">' + (place.phone || '전화번호 없음') + '</div>';

					// 목록 항목 클릭 시 해당 마커로 이동 (검색 결과 배열 인덱스 사용)
					(function(position, resultIndex) {
						item.onclick = function() {
							console.log("리스트 항목 클릭 - 인덱스:", resultIndex, "장소:", place.place_name);
							map.setCenter(position);
							map.setLevel(3);

							// 인포윈도우 열기 (검색 결과 배열 사용)
							closeAllInfowindows();
							if (searchResultInfowindows[resultIndex] && searchResultMarkers[resultIndex]) {
								searchResultInfowindows[resultIndex].open(map, searchResultMarkers[resultIndex]);
								console.log("인포윈도우 열림 - 인덱스:", resultIndex);
							} else {
								console.error("인포윈도우 또는 마커를 찾을 수 없음 - 인덱스:", resultIndex);
							}
						};
					})(placePosition, i); // i는 검색 결과에서의 정확한 인덱스

					hospitalList.appendChild(item);
				}
			} catch (e) {
				console.error("장소 표시 오류:", e, "장소:", place);
			}
		}

		// 검색된 장소 위치를 기준으로 지도 범위 재설정
		if (places.length > 0) {
			map.setBounds(bounds);
		}
	} catch (e) {
		console.error("검색 결과 표시 오류:", e);
		showMessage("검색 결과를 표시하는 중 오류가 발생했습니다.", "error");
	}
}

// 모든 마커 제거
function removeAllMarkers() {
	for (var i = 0; i < markers.length; i++) {
		markers[i].setMap(null);
	}
	markers = [];

	closeAllInfowindows();
}

// 모든 인포윈도우 닫기
function closeAllInfowindows() {
	for (var i = 0; i < infowindows.length; i++) {
		if (infowindows[i]) {
			infowindows[i].close();
		}
	}
}

// 두 지점 간의 거리 계산 (미터 단위)
function getDistance(latlng1, latlng2) {
	try {
		var lat1 = latlng1.getLat();
		var lng1 = latlng1.getLng();
		var lat2 = latlng2.getLat();
		var lng2 = latlng2.getLng();

		function deg2rad(deg) {
			return deg * (Math.PI / 180);
		}

		var R = 6371; // 지구 반경 (km)
		var dLat = deg2rad(lat2 - lat1);
		var dLng = deg2rad(lng2 - lng1);
		var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
			Math.sin(dLng / 2) * Math.sin(dLng / 2);
		var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		var distance = R * c * 1000; // 미터 단위로 변환

		return distance;
	} catch (e) {
		console.error("거리 계산 오류:", e);
		return Infinity; // 오류 발생 시 무한대 거리 반환 (필터링에서 제외되도록)
	}
}

// 메시지 표시
function showMessage(message, type) {
	try {
		var messageArea = document.getElementById('messageArea');
		if (messageArea) {
			var className = type === 'error' ? 'error-message' : 'info-message';
			messageArea.innerHTML = '<div class="' + className + '">' + message + '</div>';
		} else {
			console.warn("메시지 영역(#messageArea)을 찾을 수 없습니다");
		}
	} catch (e) {
		console.error("메시지 표시 오류:", e);
	}
}

// 메시지 초기화
function clearMessages() {
	try {
		var messageArea = document.getElementById('messageArea');
		if (messageArea) {
			messageArea.innerHTML = '';
		} else {
			console.warn("메시지 영역(#messageArea)을 찾을 수 없습니다");
		}
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}
}

