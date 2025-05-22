function fu_openAndClose(url) {
	let newWindow = window.open(url, '_blank');

	// 새 창이 열렸다면 현재 창 닫기
	if (newWindow) {
		window.close();
	} else {
		alert("팝업 차단이 되어 있어 새 창을 열 수 없습니다.");
	}
	
}