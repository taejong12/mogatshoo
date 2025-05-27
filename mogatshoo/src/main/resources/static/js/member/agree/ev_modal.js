document.addEventListener('DOMContentLoaded', function() {

	let agreeIntegration = document.getElementById('agreeIntegration');
	let inteModalBtn = document.getElementById('inteModalBtn');
	let inteContent = fu_inteHTML();

	// 통합 서비스 모달 생성 및 추가
	if (!document.getElementById('inteModal')) {
		let inteModal = document.createElement('div');
		inteModal.className = 'modal fade';
		inteModal.id = 'inteModal';
		inteModal.tabIndex = -1;

		inteModal.innerHTML = `
			<div class="modal-dialog modal-lg modal-dialog-centered">
				<div class="modal-content background-border-color">
					<div class="modal-header">
						<h3 class="modal-title title w-100 text-center fw-bold">모갓슈 통합 서비스 이용약관 동의</h3>
						<button type="button" class="btn-close closeBtn" data-bs-dismiss="modal"></button>
					</div>
					<div class="modal-body" style="max-height: 50vh; overflow-y: auto;">
						<div id="inteContent">${inteContent}</div>
					</div>
					<div class="modal-footer">
						<div class="container">
							<div class="row g-2">
								<div class="col-12 col-sm-6">
									<button type="button" class="btn modalBtn w-100" id="agreeInteBtn">동의</button>
								</div>
								<div class="col-12 col-sm-6">
									<button type="button" class="btn modalBtn w-100" data-bs-dismiss="modal">닫기</button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
        `;

		document.body.appendChild(inteModal);

		let inteModalInstance = new bootstrap.Modal(inteModal);

		// 모달 열기
		inteModalBtn.addEventListener('click', function() {
			inteModalInstance.show();
		});

		// 동의 버튼 클릭
		let agreeInteBtn = document.getElementById('agreeInteBtn');
		agreeInteBtn.addEventListener('click', function() {
			agreeIntegration.checked = true;
			agreeIntegration.value = 'Y';
			inteModalInstance.hide();
		});
	}
	
	let agreeInfo = document.getElementById('agreeInfo');
	let infoModalBtn = document.getElementById('infoModalBtn');
	let infoContent = fu_infoHTML();

	// 통합 서비스 모달 생성 및 추가
	if (!document.getElementById('infoModal')) {
		let infoModal = document.createElement('div');
		infoModal.className = 'modal fade';
		infoModal.id = 'infoModal';
		infoModal.tabIndex = -1;

		infoModal.innerHTML = `
			<div class="modal-dialog modal-lg modal-dialog-centered">
				<div class="modal-content background-border-color">
					<div class="modal-header">
						<h3 class="modal-title title w-100 text-center fw-bold">개인정보 수집 및 이용 동의</h3>
						<button type="button" class="btn-close closeBtn" data-bs-dismiss="modal"></button>
					</div>
					<div class="modal-body" style="max-height: 50vh; overflow-y: auto;">
						<div id="infoContent">${infoContent}</div>
					</div>
					<div class="modal-footer">
						<div class="container">
							<div class="row g-2">
								<div class="col-12 col-sm-6">
									<button type="button" class="btn modalBtn w-100" id="agreeInfoBtn">동의</button>
								</div>
								<div class="col-12 col-sm-6">
									<button type="button" class="btn modalBtn w-100" data-bs-dismiss="modal">닫기</button>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
        `;

		document.body.appendChild(infoModal);

		let infoModalInstance = new bootstrap.Modal(infoModal);

		// 모달 열기
		infoModalBtn.addEventListener('click', function() {
			infoModalInstance.show();
		});

		// 동의 버튼 클릭
		let agreeInfoBtn = document.getElementById('agreeInfoBtn');
		agreeInfoBtn.addEventListener('click', function() {
			agreeInfo.checked = true;
			agreeInfo.value = 'Y';
			infoModalInstance.hide();
		});
	}
});