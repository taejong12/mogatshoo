/**
 * 질문 상세 페이지 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    // 공개 상태 변경 버튼 처리
    const setupVisibilityToggle = () => {
        const toggleBtn = document.getElementById('toggleVisibilityBtn');
        const isPublicField = document.getElementById('isPublicField');
        const toggleIcon = document.getElementById('toggleIcon');
        const toggleText = document.getElementById('toggleText');
        
        if (!toggleBtn || !isPublicField) {
            console.error('공개 상태 토글 버튼 또는 필드를 찾을 수 없습니다.');
            return;
        }
        
        // 현재 상태 확인
        console.log('현재 isPublic 값:', isPublicField.value);
        
        // 토글 버튼 클릭 이벤트
        toggleBtn.addEventListener('click', function() {
            // 현재 상태 확인
            const currentValue = isPublicField.value;
            console.log('클릭 전 상태:', currentValue);
            
            // 상태 토글
            const newValue = currentValue === 'yes' ? 'no' : 'yes';
            isPublicField.value = newValue;
            console.log('변경 후 상태:', isPublicField.value);
            
            // UI 업데이트
            updateVisibilityUI(newValue);
        });
        
        // UI 상태 업데이트 함수
        function updateVisibilityUI(isPublic) {
            console.log('UI 업데이트:', isPublic);
            
            if (isPublic === 'yes') {
                // 공개 상태 UI
                toggleBtn.classList.remove('btn-success');
                toggleBtn.classList.add('btn-danger');
                toggleIcon.className = 'fas fa-eye-slash';
                toggleText.textContent = '비공개로 전환';
            } else {
                // 비공개 상태 UI
                toggleBtn.classList.remove('btn-danger');
                toggleBtn.classList.add('btn-success');
                toggleIcon.className = 'fas fa-eye';
                toggleText.textContent = '공개로 전환';
            }
        }
    };
    
    // 폼 유효성 검사
    const setupFormValidation = () => {
        const form = document.getElementById('questionForm');
        
        if (!form) return;
        
        form.addEventListener('submit', (e) => {
            let isValid = true;
            const requiredFields = form.querySelectorAll('[required]');
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('error');
                    
                    // 에러 메시지 표시
                    let errorMsg = field.parentNode.querySelector('.error-message');
                    if (!errorMsg) {
                        errorMsg = document.createElement('div');
                        errorMsg.className = 'error-message';
                        errorMsg.textContent = '이 필드는 필수입니다.';
                        field.parentNode.appendChild(errorMsg);
                    }
                } else {
                    field.classList.remove('error');
                    const errorMsg = field.parentNode.querySelector('.error-message');
                    if (errorMsg) {
                        errorMsg.remove();
                    }
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                // 첫 번째 에러 필드로 스크롤
                const firstError = form.querySelector('.error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            } else {
                // 폼 제출 시 공개 상태 값이 제대로 전송되는지 확인
                const isPublicField = document.getElementById('isPublicField');
                console.log('폼 제출 시 isPublic 값:', isPublicField?.value);
            }
        });
    };
    
    // URL 파라미터 확인해서 필요시 알림 표시
    const checkUrlParams = () => {
        const urlParams = new URLSearchParams(window.location.search);
        const status = urlParams.get('status');
        const message = urlParams.get('message');
        
        if (status && message) {
            showNotification(decodeURIComponent(message), status);
        }
    };
    
    // 알림 메시지 표시
    const showNotification = (message, type = 'info') => {
        // 이미 존재하는 알림이 있으면 제거
        const existingNotif = document.querySelector('.notification');
        if (existingNotif) {
            existingNotif.remove();
        }
        
        // 알림 요소 생성
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
                <span>${message}</span>
            </div>
            <button class="notification-close">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        // 알림 닫기 버튼
        const closeBtn = notification.querySelector('.notification-close');
        closeBtn.addEventListener('click', () => {
            notification.classList.add('hide');
            setTimeout(() => {
                notification.remove();
            }, 300);
        });
        
        // 자동으로 4초 후 사라짐
        setTimeout(() => {
            notification.classList.add('hide');
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 4000);
        
        // 문서에 알림 추가
        document.body.appendChild(notification);
        
        // 애니메이션 효과를 위한 지연
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);
    };
    
    // 페이지 로드 시 실행되는 초기화 함수
    const initDetailPage = () => {
        setupVisibilityToggle();
        setupFormValidation();
        checkUrlParams();
    };
    
    initDetailPage();
});