// ev_kakaoMapDomLoad.js
document.addEventListener('DOMContentLoaded', function() {
  console.log("DOM 로드 완료");
  
  // 로딩 표시
  if (typeof showLoading === 'function') {
    showLoading(true);
  } else {
    console.error("showLoading 함수를 찾을 수 없습니다");
    if (document.getElementById('loading')) {
      document.getElementById('loading').style.display = 'flex';
    }
  }
  
  // 안전장치 - 30초 후에 강제로 로딩 숨기기
  setTimeout(function() {
    if (document.getElementById('loading').style.display !== 'none') {
      console.log("안전장치 작동: 로딩 시간 초과");
      if (typeof showLoading === 'function') {
        showLoading(false);
      } else {
        document.getElementById('loading').style.display = 'none';
      }
      
      if (typeof showMessage === 'function') {
        showMessage("로딩 시간이 초과되었습니다. 페이지를 새로고침하거나 다시 시도해 주세요.", "error");
      } else {
        console.error("showMessage 함수를 찾을 수 없습니다");
      }
    }
  }, 30000);

  // 위치 정보 초기화 및 지도 로드
  if (typeof getLocationAndInitMap === 'function') {
    getLocationAndInitMap();
  } else {
    console.error("getLocationAndInitMap 함수를 찾을 수 없습니다");
  }
});