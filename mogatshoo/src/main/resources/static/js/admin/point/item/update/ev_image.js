document.getElementById('imgFile').addEventListener('change', function(event) {
	let file = event.target.files[0];
	let previewImg = document.getElementById('previewImage');

	if (file && previewImg) {
		let reader = new FileReader();
		reader.onload = function(e) {
			// 기존 이미지에 덮어쓰기
			previewImg.src = e.target.result;
		};
		reader.readAsDataURL(file);
	}
});