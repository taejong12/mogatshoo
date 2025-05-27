// ev_joinCheck.js 내용
document.addEventListener('DOMContentLoaded', function() {
    console.log("모달 스크립트 로드됨");
    
    // 개인정보 동의 섹션과 체크박스 참조
    const privacyBox = document.getElementById('privacyBox');
    const checkbox = document.getElementById('memberInfoCheck');
    const infoCheckContainer = document.querySelector('.infoCheckContainer');
    let openModalBtn = document.getElementById('openModalBtn');
	
    if (!privacyBox || !checkbox || !infoCheckContainer) {
        console.error("필요한 요소를 찾을 수 없습니다");
        return;
    }
    
    // 개인정보 박스 내용 백업 후 숨기기
    const privacyContent = privacyBox.innerHTML;
    privacyBox.style.display = 'none';
    
    // 모달 생성 및 추가
    if (!document.getElementById('privacyModal')) {
        const modal = document.createElement('div');
        modal.id = 'privacyModal';
        modal.className = 'privacy-modal';
        modal.style.cssText = 'display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.7); z-index: 1000; justify-content: center; align-items: center;';
        
        modal.innerHTML = `
            <div class="modal-content" style="background-color: rgb(54,45,62); border: 5px solid rgb(195,54,38); padding: 20px; width: 80%; max-width: 500px; max-height: 80%; overflow-y: auto; color: white; position: relative;">
                <span class="close-modal" style="position: absolute; top: 10px; right: 10px; color: rgb(255, 255, 0); font-size: 24px; cursor: pointer;">&times;</span>
                <h3 style="color: rgb(255, 255, 0); margin-bottom: 15px;">개인정보 수집·이용 동의</h3>
                <div>${privacyContent}</div>
                <button type="button" id="agreeBtn" style="background-color: rgb(54,45,62); border: 2px solid rgb(195,54,38); color: rgb(255, 255, 0); padding: 5px 10px; margin-top: 15px; cursor: pointer;">동의합니다</button>
            </div>
        `;
        
        document.body.appendChild(modal);
        
        // 이벤트 리스너 등록
        
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
            checkbox.checked = true;
            modal.style.display = 'none';
        });
    }
    
    console.log("모달 스크립트 초기화 완료");
});