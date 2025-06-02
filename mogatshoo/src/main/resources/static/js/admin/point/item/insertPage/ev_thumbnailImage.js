document.getElementById('imgFile').addEventListener('change', function(event) {
	let file = event.target.files[0];
	let previewImage = document.getElementById('thumbnailImage');

	if (file && file.type.startsWith('image/')) {
		let reader = new FileReader();
		reader.onload = function(e) {
			previewImage.src = e.target.result;
			// 이미지 보여주기
			previewImage.classList.remove('d-none');
		};
		// 이미지 파일 → base64 인코딩
		reader.readAsDataURL(file);
	} else {
		previewImage.src = '#';
		// 이미지 숨기기
		previewImage.classList.add('d-none');
	}
});