// 개인정보수집동의
document.addEventListener('DOMContentLoaded', function() {
	const privacyBox = document.getElementById('privacyBox');
	const checkBox = document.getElementById('memberInfoCheck');

	privacyBox.addEventListener('scroll', function() {
		// 스크롤 끝까지 내렸는지 확인
		
		// 1. scrollTop
		// -> 스크롤이 얼마나 아래로 내려갔는지 픽셀 단위 값
		// -> 0: 스크롤 안 내림(맨위)
		// -> 30: 위에서 아래로 30px 내림
		// -> 계속 증가 -> 얼마나 내렸는지 거리
		
		// 2. clientHeight
		// -> 눈에 실제로 보이는 div의 높이 (padding 포함, border 제외)
		
		// 3. scrollHeight
		// -> div 안에 실제로 존재하는 전체 컨텐츠의 총 높이
		if (privacyBox.scrollTop >= privacyBox.clientHeight) {
			checkBox.disabled = false;
		}
	});
});