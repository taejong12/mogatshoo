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
  });
}