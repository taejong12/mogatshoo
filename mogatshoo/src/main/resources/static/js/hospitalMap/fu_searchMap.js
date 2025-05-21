
// 기본 위치(서울시청)에서 검색
function fallbackToDefaultLocation() {
	currentPosition = new kakao.maps.LatLng(37.566826, 126.9786567);
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

	searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);
}

// 여러 키워드로 순차적으로 검색 시도
function searchMultipleKeywords(keywords, index = 0) {
	// 이전 타임아웃 제거
	if (searchTimeout) {
		clearTimeout(searchTimeout);
	}

	// 새 타임아웃 설정 (10초)
	searchTimeout = setTimeout(function() {
		console.log("검색 타임아웃 발생");
		showMessage("검색 시간이 초과되었습니다. 다시 시도해 주세요.", "error");
		showLoading(false);
	}, 10000);

	if (index >= keywords.length) {
		// 모든 키워드로 검색했지만 결과가 없음
		clearTimeout(searchTimeout);
		if (markers.length === 0) {
			showMessage("주변에 탈모 관련 병원을 찾을 수 없습니다. 검색 반경을 넓히거나 다른 키워드로 검색해보세요.", "error");
			document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
		}
		showLoading(false);
		return;
	}

	var position = currentPosition || map.getCenter();
	var radius = document.getElementById('radius').value;

	console.log("검색 키워드:", keywords[index]);

	var places = new kakao.maps.services.Places();
	places.keywordSearch(keywords[index], function(result, status) {
		console.log("검색 결과 상태:", status, "결과 수:", result ? result.length : 0);

		if (status === kakao.maps.services.Status.OK && result && result.length > 0) {
			// 거리에 따라 결과 필터링
			var filteredResults = result.filter(function(place) {
				var placePosition = new kakao.maps.LatLng(place.y, place.x);
				var distance = getDistance(position, placePosition);
				return distance <= radius;
			});

			console.log("필터링된 결과 수:", filteredResults.length);

			if (filteredResults.length > 0) {
				// 타임아웃 제거
				clearTimeout(searchTimeout);
				// 결과가 있으면 표시
				displaySearchResults(filteredResults);
				if (isFirstSearch) {
					showMessage(filteredResults.length + "개의 " + keywords[index] + " 검색 결과를 찾았습니다.", "info");
					isFirstSearch = false;
				}
				showLoading(false);
			} else {
				// 결과가 없으면 다음 키워드로 검색
				searchMultipleKeywords(keywords, index + 1);
			}
		} else {
			// 검색 실패하면 다음 키워드로 검색
			console.log("키워드 검색 실패 또는 결과 없음, 다음 키워드로 넘어감");
			searchMultipleKeywords(keywords, index + 1);
		}
	}, {
		location: position,
		radius: radius,
		sort: kakao.maps.services.SortBy.DISTANCE
	});
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
			document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
		}
		showLoading(false);
	});
}

// 카테고리로 검색
function searchByCategory(category) {
	showLoading(true);
	try {
		clearMessages();
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}

	var position = currentPosition || map.getCenter();
	var radius = document.getElementById('radius').value;

	removeAllMarkers();

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
			// 위치 기반 필터링
			var filteredResults = result.filter(function(place) {
				var placePosition = new kakao.maps.LatLng(place.y, place.x);
				var distance = getDistance(position, placePosition);
				return distance <= radius;
			});

			if (filteredResults.length > 0) {
				displaySearchResults(filteredResults);
				showMessage(filteredResults.length + "개의 " + category + " 검색 결과를 찾았습니다.", "info");
			} else {
				showMessage("주변에 " + category + "을(를) 찾을 수 없습니다. 검색 반경을 넓혀보세요.", "error");
				document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
			}
		} else {
			showMessage("검색 결과가 없습니다. 다른 키워드로 검색해보세요.", "error");
			document.getElementById('hospital-list').innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
		}
		showLoading(false);
	}, {
		location: position,
		radius: radius,
		sort: kakao.maps.services.SortBy.DISTANCE
	});
}

// 검색 결과 표시
function displaySearchResults(places) {
	try {
		var bounds = new kakao.maps.LatLngBounds();
		var hospitalList = document.getElementById('hospital-list');
		hospitalList.innerHTML = '';

		console.log("표시할 장소 수:", places.length);

		for (var i = 0; i < places.length; i++) {
			var place = places[i];
			try {
				displayMarker(place);

				var placePosition = new kakao.maps.LatLng(place.y, place.x);
				bounds.extend(placePosition);

				// 병원 목록 아이템 생성
				var item = document.createElement('div');
				item.className = 'hospital-item';
				item.innerHTML = '<div class="hospital-name">' + place.place_name + '</div>' +
					'<div class="hospital-address">' + place.address_name + '</div>' +
					'<div class="hospital-phone">' + (place.phone || '전화번호 없음') + '</div>';

				// 목록 항목 클릭 시 해당 마커로 이동
				(function(position, index) {
					item.onclick = function() {
						map.setCenter(position);
						map.setLevel(3);

						// 인포윈도우 열기
						closeAllInfowindows();
						if (infowindows[index]) {
							infowindows[index].open(map, markers[index]);
						}
					};
				})(placePosition, i);

				hospitalList.appendChild(item);
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

// 마커 표시
function displayMarker(place) {
	try {
		var marker = new kakao.maps.Marker({
			map: map,
			position: new kakao.maps.LatLng(place.y, place.x)
		});

		markers.push(marker);

		var infoContent = '<div style="padding:10px;min-width:200px;">' +
			'<h5 style="margin-top:5px;margin-bottom:5px;">' + place.place_name + '</h5>' +
			'<div style="font-size:13px;color:#666;margin-bottom:5px;">' + place.address_name + '</div>';

		if (place.phone) {
			infoContent += '<div style="font-size:13px;color:#666;margin-bottom:5px;">☎ ' + place.phone + '</div>';
		}

		if (place.place_url) {
			infoContent += '<a href="' + place.place_url + '" target="_blank" style="color:#007bff;font-size:13px;">상세 정보 보기</a>';
		}

		infoContent += '</div>';

		var infowindow = new kakao.maps.InfoWindow({
			content: infoContent
		});

		infowindows.push(infowindow);

		kakao.maps.event.addListener(marker, 'click', function() {
			closeAllInfowindows();
			infowindow.open(map, marker);
		});
	} catch (e) {
		console.error("마커 표시 오류:", e, "장소:", place);
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
