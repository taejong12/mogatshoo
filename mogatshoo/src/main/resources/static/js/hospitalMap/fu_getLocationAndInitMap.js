// 현재 위치를 가져온 다음 지도 초기화
function getLocationAndInitMap() {
  if (navigator.geolocation) {
    // 위치 가져오기 타임아웃 설정 (15초)
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
        
        // 위도와 경도 저장 (LatLng 객체는 아직 만들지 않음)
        userLat = position.coords.latitude;
        userLng = position.coords.longitude;
        
        // 카카오맵 API를 로드한 후 위치 사용
        loadKakaoMapWithPosition();
      },
      function(error) {
        clearTimeout(locationTimeout); // 타임아웃 제거
        console.error("위치 가져오기 오류:", error);
        showMessage("위치 정보를 가져오는데 실패했습니다. 기본 위치 없이 시작합니다.", "error");
        // 기본 위치 없이 지도 초기화
        loadKakaoMapWithoutPosition();
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  } else {
    showMessage("이 브라우저에서는 위치 정보를 지원하지 않습니다. 기본 위치 없이 시작합니다.", "error");
    // 기본 위치 없이 지도 초기화
    loadKakaoMapWithoutPosition();
  }
}