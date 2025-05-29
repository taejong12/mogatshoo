// 전역 변수
var map;
var currentPosition = null;
var markers = [];
var infowindows = [];
var searchTimeout;
var isFirstSearch = true;
var userLat, userLng;
var radiusCircle = null;

// DOM 로드 완료 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM 로드 완료");
    showLoading(true);
    
    // 30초 후 강제 로딩 숨기기
    setTimeout(function() {
        if (document.getElementById('loading').style.display !== 'none') {
            showLoading(false);
            showMessage("로딩 시간이 초과되었습니다. 페이지를 새로고침해 주세요.", "error");
        }
    }, 30000);
    
    // 현재 위치 가져오기 시작
    getLocationAndInitMap();
});

// 현재 위치 가져오기 및 지도 초기화
function getLocationAndInitMap() {
    if (navigator.geolocation) {
        var locationTimeout = setTimeout(function() {
            console.log("위치 정보 타임아웃 - 기본 위치로 시작");
            loadKakaoMapWithoutPosition();
        }, 15000);

        navigator.geolocation.getCurrentPosition(
            function(position) {
                clearTimeout(locationTimeout);
                console.log("위치 가져오기 성공:", position.coords.latitude, position.coords.longitude);
                
                userLat = position.coords.latitude;
                userLng = position.coords.longitude;
                loadKakaoMapWithPosition();
            },
            function(error) {
                clearTimeout(locationTimeout);
                console.error("위치 가져오기 실패:", error);
                showMessage("위치 정보를 가져올 수 없습니다. 기본 위치로 시작합니다.", "error");
                loadKakaoMapWithoutPosition();
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    } else {
        showMessage("이 브라우저는 위치 정보를 지원하지 않습니다.", "error");
        loadKakaoMapWithoutPosition();
    }
}

// 위치 있음: 지도 로드
function loadKakaoMapWithPosition() {
    kakao.maps.load(function() {
        console.log("카카오맵 로드 완료 (위치 있음)");
        
        currentPosition = new kakao.maps.LatLng(userLat, userLng);
        
        var container = document.getElementById('map');
        var options = {
            center: currentPosition,
            level: 4
        };
        
        map = new kakao.maps.Map(container, options);
        
        // 현재 위치 마커
        addCurrentLocationMarker();
        
        // 반경 변경 이벤트
        setupRadiusChangeEvent();
        
        // 반경 시각화
        updateSearchRadius();
        
        // 병원 검색
        searchAllKeywords();
        
        showLoading(false);
    });
}

// 위치 없음: 지도 로드
function loadKakaoMapWithoutPosition() {
    kakao.maps.load(function() {
        console.log("카카오맵 로드 완료 (위치 없음)");
        
        var container = document.getElementById('map');
        var options = {
            center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 중심
            level: 8
        };
        
        map = new kakao.maps.Map(container, options);
        
        setupRadiusChangeEvent();
        showMessage("현재 위치를 사용할 수 없습니다. 직접 검색해주세요.", "info");
        showLoading(false);
    });
}

// 현재 위치 마커 추가
function addCurrentLocationMarker() {
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
}

// 반경 변경 이벤트 설정
function setupRadiusChangeEvent() {
    var radiusSelect = document.getElementById('radius');
    if (radiusSelect) {
        radiusSelect.addEventListener('change', function() {
            if (map && currentPosition) {
                updateSearchRadius();
                showLoading(true);
                removeSearchMarkers();
                addCurrentLocationMarker();
                searchAllKeywords();
                showMessage("반경 " + (this.value / 1000) + "km로 재검색합니다.", "info");
            }
        });
    }
}

// 검색 반경 시각화
function updateSearchRadius() {
    if (radiusCircle) {
        radiusCircle.setMap(null);
    }
    
    if (!currentPosition) return;
    
    var radius = parseInt(document.getElementById('radius').value, 10);
    
    radiusCircle = new kakao.maps.Circle({
        center: currentPosition,
        radius: radius,
        strokeWeight: 3,
        strokeColor: '#FF3333',
        strokeOpacity: 0.8,
        fillColor: '#FF9999',
        fillOpacity: 0.2
    });
    
    radiusCircle.setMap(map);
}

// 모든 키워드로 통합 검색
function searchAllKeywords() {
    if (!currentPosition) {
        showMessage("현재 위치가 설정되지 않았습니다.", "error");
        showLoading(false);
        return;
    }
    
    const keywords = ['탈모 병원', '두피 클리닉', '피부과', '모발이식'];
    const radius = parseInt(document.getElementById('radius').value, 10);
    
    console.log("🔍 통합 검색 시작 - 반경:", radius, "미터");
    
    let completedSearches = 0;
    let allResults = [];
    const seenPlaces = new Set();
    
    // 타임아웃 설정
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = setTimeout(function() {
        showMessage("검색 시간이 초과되었습니다.", "error");
        showLoading(false);
    }, 15000);
    
    keywords.forEach((keyword) => {
        const places = new kakao.maps.services.Places();
        
        places.keywordSearch(keyword, function(result, status) {
            completedSearches++;
            console.log(`${keyword}: ${result ? result.length : 0}개 결과`);
            
            if (status === kakao.maps.services.Status.OK && result) {
                result.forEach(place => {
                    if (!seenPlaces.has(place.id)) {
                        seenPlaces.add(place.id);
                        place.searchKeyword = keyword;
                        allResults.push(place);
                    }
                });
            }
            
            // 모든 검색 완료시
            if (completedSearches === keywords.length) {
                clearTimeout(searchTimeout);
                
                if (allResults.length > 0) {
                    // 거리순 정렬
                    allResults.sort((a, b) => {
                        const distA = getDistance(currentPosition, new kakao.maps.LatLng(a.y, a.x));
                        const distB = getDistance(currentPosition, new kakao.maps.LatLng(b.y, b.x));
                        return distA - distB;
                    });
                    
                    displaySearchResults(allResults);
                    showMessage(`${allResults.length}개의 병원을 찾았습니다.`, "info");
                } else {
                    showMessage("검색 결과가 없습니다. 반경을 넓혀보세요.", "error");
                    clearHospitalList();
                }
                showLoading(false);
            }
        }, {
            location: currentPosition,
            radius: radius,
            sort: kakao.maps.services.SortBy.DISTANCE
        });
    });
}

// 검색 결과 표시
function displaySearchResults(places) {
    var bounds = new kakao.maps.LatLngBounds();
    var hospitalList = document.getElementById('hospital-list');
    
    if (hospitalList) {
        hospitalList.innerHTML = '';
    }
    
    if (currentPosition) {
        bounds.extend(currentPosition);
    }
    
    for (var i = 0; i < places.length; i++) {
        var place = places[i];
        var placePosition = new kakao.maps.LatLng(place.y, place.x);
        
        // 마커 생성
        var marker = new kakao.maps.Marker({
            map: map,
            position: placePosition
        });
        markers.push(marker);
        bounds.extend(placePosition);
        
        // 거리 계산
        var distance = currentPosition ? getDistance(currentPosition, placePosition) : 0;
        var distanceText = distance >= 1000 
            ? (distance / 1000).toFixed(1) + 'km'
            : Math.round(distance) + 'm';
        
        // 인포윈도우 내용
        var infoContent = 
            '<div style="padding:15px;min-width:250px;">' +
            '<h5 style="margin:0 0 8px 0;">' + place.place_name + '</h5>' +
            '<div style="font-size:12px;color:#FF3333;margin-bottom:5px;">거리: ' + distanceText + '</div>' +
            '<div style="font-size:11px;color:#007bff;margin-bottom:5px;">🔍 ' + place.searchKeyword + '</div>' +
            '<div style="font-size:14px;margin-bottom:5px;">' + place.address_name + '</div>';
        
        if (place.phone) {
            infoContent += '<div style="font-size:14px;margin-bottom:8px;">📞 ' + place.phone + '</div>';
        }
        
        if (place.place_url) {
            infoContent += '<div><a href="' + place.place_url + '" target="_blank" style="color:#007bff;">상세정보</a></div>';
        }
        
        infoContent += '</div>';
        
        var infowindow = new kakao.maps.InfoWindow({
            content: infoContent,
            removable: true
        });
        infowindows.push(infowindow);
        
        // 마커 클릭 이벤트
        (function(m, info) {
            kakao.maps.event.addListener(m, 'click', function() {
                closeAllInfowindows();
                info.open(map, m);
            });
        })(marker, infowindow);
        
        // 병원 목록 추가
        if (hospitalList) {
            var item = document.createElement('div');
            item.className = 'hospital-item p-3 border-bottom';
            item.style.cursor = 'pointer';
            
            item.innerHTML = 
                '<div class="fw-bold">' + place.place_name + ' <span class="badge bg-primary">' + distanceText + '</span></div>' +
                '<div class="text-muted small">' + place.address_name + '</div>' +
                '<div class="text-muted small">' + (place.phone || '전화번호 없음') + '</div>' +
                '<div class="badge bg-light text-dark">' + place.searchKeyword + '</div>';
            
            // 목록 클릭시 지도 이동
            (function(pos, idx) {
                item.onclick = function() {
                    map.setCenter(pos);
                    map.setLevel(3);
                    closeAllInfowindows();
                    infowindows[idx].open(map, markers[idx + 1]); // +1은 현재위치 마커 때문
                };
            })(placePosition, i);
            
            hospitalList.appendChild(item);
        }
    }
    
    if (places.length > 0) {
        map.setBounds(bounds);
    }
}

// 키워드 검색
function searchByKeyword() {
    var keyword = document.getElementById('keyword').value.trim();
    if (!keyword) {
        showMessage("검색어를 입력해주세요.", "error");
        return;
    }
    
    showLoading(true);
    removeSearchMarkers();
    if (currentPosition) addCurrentLocationMarker();
    
    var places = new kakao.maps.services.Places();
    places.keywordSearch(keyword, function(result, status) {
        if (status === kakao.maps.services.Status.OK && result.length > 0) {
            displaySearchResults(result);
            showMessage(result.length + "개의 검색 결과를 찾았습니다.", "info");
        } else {
            showMessage("검색 결과가 없습니다.", "error");
            clearHospitalList();
        }
        showLoading(false);
    });
}

// 카테고리 검색
function searchByCategory(category) {
    showLoading(true);
    removeSearchMarkers();
    if (currentPosition) addCurrentLocationMarker();
    
    var position = currentPosition || map.getCenter();
    var radius = parseInt(document.getElementById('radius').value, 10);
    
    var places = new kakao.maps.services.Places();
    places.keywordSearch(category, function(result, status) {
        if (status === kakao.maps.services.Status.OK && result.length > 0) {
            displaySearchResults(result);
            showMessage(result.length + "개의 " + category + " 결과를 찾았습니다.", "info");
        } else {
            showMessage("검색 결과가 없습니다.", "error");
            clearHospitalList();
        }
        showLoading(false);
    }, {
        location: position,
        radius: radius,
        sort: kakao.maps.services.SortBy.DISTANCE
    });
}

// 현재 위치 재검색
function getCurrentLocation() {
    showLoading(true);
    showMessage("위치 정보를 가져오는 중...", "info");
    
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                userLat = position.coords.latitude;
                userLng = position.coords.longitude;
                currentPosition = new kakao.maps.LatLng(userLat, userLng);
                
                map.setCenter(currentPosition);
                map.setLevel(4);
                
                removeAllMarkers();
                addCurrentLocationMarker();
                updateSearchRadius();
                searchAllKeywords();
                
                showMessage("현재 위치에서 병원을 검색합니다.", "info");
            },
            function(error) {
                showMessage("위치 정보를 가져올 수 없습니다.", "error");
                showLoading(false);
            }
        );
    }
}

// 유틸리티 함수들
function removeSearchMarkers() {
    // 현재 위치 마커 제외하고 제거
    for (var i = 1; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers = markers.slice(0, 1); // 첫 번째(현재위치) 마커만 유지
    closeAllInfowindows();
}

function removeAllMarkers() {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers = [];
    closeAllInfowindows();
}

function closeAllInfowindows() {
    for (var i = 0; i < infowindows.length; i++) {
        infowindows[i].close();
    }
}

function getDistance(latlng1, latlng2) {
    var lat1 = latlng1.getLat();
    var lng1 = latlng1.getLng();
    var lat2 = latlng2.getLat();
    var lng2 = latlng2.getLng();
    
    function deg2rad(deg) {
        return deg * (Math.PI / 180);
    }
    
    var R = 6371;
    var dLat = deg2rad(lat2 - lat1);
    var dLng = deg2rad(lng2 - lng1);
    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return R * c * 1000; // 미터 단위
}

function showLoading(show) {
    var loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = show ? 'flex' : 'none';
    }
}

function showMessage(message, type) {
    var messageArea = document.getElementById('messageArea');
    if (messageArea) {
        var className = type === 'error' ? 'alert alert-danger' : 'alert alert-info';
        messageArea.innerHTML = '<div class="' + className + ' mt-2">' + message + '</div>';
        
        // 3초 후 메시지 자동 제거
        setTimeout(function() {
            messageArea.innerHTML = '';
        }, 3000);
    }
}

function clearHospitalList() {
    var hospitalList = document.getElementById('hospital-list');
    if (hospitalList) {
        hospitalList.innerHTML = '<div class="text-center p-4"><p class="text-muted">검색 결과가 없습니다.</p></div>';
    }
}