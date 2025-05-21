// 현재 위치로 지도 초기화
function initMapWithPosition(position) {
  kakao.maps.load(function() {
    console.log("카카오맵 API 로드 완료");
    
    var container = document.getElementById('map');
    var options = {
      center: position,
      level: 4
    };
    
    map = new kakao.maps.Map(container, options);
    
    // 현재 위치 마커 표시
    var marker = new kakao.maps.Marker({
      map: map,
      position: position,
      image: new kakao.maps.MarkerImage(
        'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png',
        new kakao.maps.Size(24, 35)
      )
    });
    
    markers.push(marker);
    
    // 주변 병원 검색
    searchMultipleKeywords(['탈모 병원', '두피 클리닉', '피부과', '모발이식']);
  });
}

// 기본 위치 없이 지도 초기화 (대한민국 중심으로 확대)
function initMapWithoutPosition() {
  kakao.maps.load(function() {
    console.log("카카오맵 API 로드 완료 (위치 없음)");
    
    var container = document.getElementById('map');
    var options = {
      center: new kakao.maps.LatLng(36.2, 127.9), // 대한민국 중심 좌표 (대략)
      level: 13 // 전국이 보이는 레벨
    };
    
    map = new kakao.maps.Map(container, options);
    showMessage("현재 위치를 사용할 수 없습니다. 검색창에서 지역을 직접 검색해주세요.", "info");
    showLoading(false);
  });
}