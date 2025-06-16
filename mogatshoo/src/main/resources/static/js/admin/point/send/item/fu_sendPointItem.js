function fu_sendPointItem() {
	let sendPointItemForm = document.sendPointItemForm;

	let imgFile = document.getElementById('imgFile');
	let imgWarnMsg = document.getElementById('imgWarnMsg');

	imgWarnMsg.textContent = '';
	imgWarnMsg.style.display = 'none';

	let file = imgFile.files[0];

	if (!file) {
		imgFile.focus();
		fu_showWarning(imgWarnMsg, '파일이 존재하지 않습니다. 등록해주세요.');
		return;
	}

	if (!file.type.startsWith("image/")) {
		imgFile.focus();
		fu_showWarning(imgWarnMsg, '이미지 파일만 업로드할 수 있습니다.');
		return;
	}

	sendPointItemForm.method = "post";
	sendPointItemForm.action = "/admin/point/send/item";
	sendPointItemForm.submit();
}