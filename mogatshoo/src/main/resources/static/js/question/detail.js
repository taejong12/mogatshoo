document.addEventListener('DOMContentLoaded', function() {
    // 공개 상태 토글 버튼 이벤트
    const publicBtn = document.querySelector('.visibility-btn.public');
    const privateBtn = document.querySelector('.visibility-btn.private');
    const isPublicInput = document.getElementById('isPublicInput');
    const currentStatus = document.getElementById('currentStatus');
    const serialNumber = document.querySelector('input[name="serialNumber"]').value;
    
    // 공개 버튼 클릭
    publicBtn.addEventListener('click', function() {
        updateVisibility('yes', this);
    });
    
    // 비공개 버튼 클릭
    privateBtn.addEventListener('click', function() {
        updateVisibility('no', this);
    });
    
    // 공개 상태 업데이트 함수
    function updateVisibility(value, clickedBtn) {
        // UI 업데이트
        if (value === 'yes') {
            publicBtn.classList.add('active');
            privateBtn.classList.remove('active');
            currentStatus.textContent = '공개';
        } else {
            publicBtn.classList.remove('active');
            privateBtn.classList.add('active');
            currentStatus.textContent = '비공개';
        }
        
        // 폼 입력값 업데이트
        isPublicInput.value = value;
        
        // AJAX 요청 (즉시 서버에 상태 변경 알림 - 선택사항)
        fetch(`/questions/${serialNumber}/visibility`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `isPublic=${value}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast(data.message, 'success');
            } else {
                showToast(data.message || '상태 변경 중 오류가 발생했습니다.', 'danger');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('네트워크 오류가 발생했습니다.', 'danger');
        });
    }
    
    // 토스트 메시지 표시 함수
    function showToast(message, type = 'info') {
        const toastEl = document.getElementById('statusToast');
        const toast = new bootstrap.Toast(toastEl);
        
        document.getElementById('toastMessage').textContent = message;
        
        // 토스트 배경색 설정
        toastEl.classList.remove('bg-success', 'bg-danger', 'bg-info');
        if (type === 'success') {
            toastEl.classList.add('bg-success', 'text-white');
        } else if (type === 'danger') {
            toastEl.classList.add('bg-danger', 'text-white');
        } else {
            toastEl.classList.add('bg-info', 'text-white');
        }
        
        toast.show();
    }
    
    // URL 파라미터에서 상태 메시지 확인
    const urlParams = new URLSearchParams(window.location.search);
    const status = urlParams.get('status');
    const message = urlParams.get('message');
    
    if (status && message) {
        showToast(message, status === 'success' ? 'success' : 'danger');
    }
});