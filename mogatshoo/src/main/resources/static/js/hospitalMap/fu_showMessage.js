
// 메시지 표시
function showMessage(message, type) {
	try {
		var messageArea = document.getElementById('messageArea');
		if (messageArea) {
			var className = type === 'error' ? 'error-message' : 'info-message';
			messageArea.innerHTML = '<div class="' + className + '">' + message + '</div>';
		} else {
			console.error("메시지 영역을 찾을 수 없습니다.");
		}
	} catch (e) {
		console.error("메시지 표시 오류:", e);
	}
}

// 메시지 초기화
function clearMessages() {
	try {
		var messageArea = document.getElementById('messageArea');
		if (messageArea) {
			messageArea.innerHTML = '';
		} else {
			console.error("메시지 영역을 찾을 수 없습니다.");
		}
	} catch (e) {
		console.error("메시지 초기화 오류:", e);
	}
}

// 로딩 표시
function showLoading(show) {
	try {
		var loadingElement = document.getElementById('loading');
		if (loadingElement) {
			loadingElement.style.display = show ? 'flex' : 'none';
		} else {
			console.error("로딩 영역을 찾을 수 없습니다.");
		}
	} catch (e) {
		console.error("로딩 표시 오류:", e);
	}
}