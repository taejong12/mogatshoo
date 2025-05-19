// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 개인정보 동의 섹션을 수정
    const privacySection = document.querySelector('.input-wrap.d-flex.flex-column');
    const privacyBox = document.getElementById('privacyBox');
    const privacyContent = privacyBox.innerHTML;
    
    // 기존 컨텐츠 제거
    privacySection.innerHTML = '';
    
    // 모달 열기 버튼 추가
    const openModalBtn = document.createElement('button');
    openModalBtn.type = 'button';
    openModalBtn.className = 'privacy-modal-btn';
    openModalBtn.textContent = '개인정보 수집·이용 동의 확인';
    privacySection.appendChild(openModalBtn);
    
    // 체크박스 추가
    const checkboxContainer = document.createElement('div');
    checkboxContainer.className = 'info-input-wrap d-flex align-items-center';
    checkboxContainer.innerHTML = `
        <input type="checkbox" id="memberInfoCheck" name="memberInfoCheck" required>
        <label for="memberInfoCheck">위 내용을 읽고 동의합니다.</label>
    `;
    privacySection.appendChild(checkboxContainer);
    
    // 모달 생성
    const modal = document.createElement('div');
    modal.className = 'privacy-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <h3 style="color: rgb(255, 255, 0); margin-bottom: 15px;">개인정보 수집·이용 동의</h3>
            <div>${privacyContent}</div>
            <button type="button" id="agreeBtn" style="margin-top: 15px;">동의합니다</button>
        </div>
    `;
    document.body.appendChild(modal);
    
    // 모달 열기
    openModalBtn.addEventListener('click', function() {
        modal.style.display = 'flex';
    });
    
    // 모달 닫기 (X 버튼)
    const closeBtn = modal.querySelector('.close-modal');
    closeBtn.addEventListener('click', function() {
        modal.style.display = 'none';
    });
    
    // 모달 닫기 (바깥 영역 클릭)
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });
    
    // 동의 버튼 클릭
    const agreeBtn = document.getElementById('agreeBtn');
    agreeBtn.addEventListener('click', function() {
        document.getElementById('memberInfoCheck').checked = true;
        modal.style.display = 'none';
    });
});