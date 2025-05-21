
// 지도 초기화
function initMap() {
	try {
		var container = document.getElementById('map');
		var options = {
			center: new kakao.maps.LatLng(37.566826, 126.9786567), // 서울 시청
			level: 5
		};
		map = new kakao.maps.Map(container, options);
		console.log("지도 초기화 완료");
	} catch (e) {
		console.error("지도 초기화 오류:", e);
		showMessage("지도를 불러오는 중 오류가 발생했습니다. 페이지를 새로고침해 주세요.", "error");
		showLoading(false);
	}
}