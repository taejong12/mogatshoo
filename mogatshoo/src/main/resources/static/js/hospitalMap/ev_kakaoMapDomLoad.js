// DOM 요소가 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
  console.log("DOM 로드 완료");
  showLoading(true);
  
  // 안전장치 - 30초 후에 강제로 로딩 숨기기
  setTimeout(function() {
    if (document.getElementById('loading').style.display !== 'none') {
      console.log("안전장치 작동: 로딩 시간 초과");
      showLoading(false);
      showMessage("로딩 시간이 초과되었습니다. 페이지를 새로고침하거나 다시 시도해 주세요.", "error");
    }
  }, 30000);

  // 먼저 현재 위치를 가져온 다음 지도 초기화
  getLocationAndInitMap();
});