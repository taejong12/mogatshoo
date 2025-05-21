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