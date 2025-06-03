// 경고메시지 함수
function fu_showWarning(warnElement, message) {
	warnElement.textContent = message;
	warnElement.style.color = 'rgb(255, 107, 107)';
	warnElement.style.fontSize = '0.8em';
	warnElement.style.padding = '.375rem .75rem';
	warnElement.style.display = 'inline';
}