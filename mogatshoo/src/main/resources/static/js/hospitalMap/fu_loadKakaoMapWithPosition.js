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
    
    // 현재 위치 마커 표시
    var marker = new kakao.maps.Marker({
      map: map,
      position: currentPosition,
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