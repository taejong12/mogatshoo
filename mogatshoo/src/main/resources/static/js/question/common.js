/**
 * 공통 JavaScript 함수
 */
document.addEventListener('DOMContentLoaded', function() {
    // 모바일 메뉴 토글 기능 (필요시 구현)
    const setupMobileMenu = () => {
        const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
        const mainNav = document.querySelector('.main-nav');
        
        if (mobileMenuToggle) {
            mobileMenuToggle.addEventListener('click', () => {
                mainNav.classList.toggle('active');
            });
        }
    };
    
    // 페이지 로드 시 실행되는 공통 함수들
    const initCommon = () => {
        setupMobileMenu();
    };
    
    initCommon();
});

/**
 * 알림 메시지 표시 함수
 * @param {string} message - 표시할 메시지
 * @param {string} type - 알림 유형 ('success', 'error', 'info')
 */
function showNotification(message, type = 'info') {
    // 이미 존재하는 알림이 있으면 제거
    const existingNotif = document.querySelector('.notification');
    if (existingNotif) {
        existingNotif.remove();
    }
    
    // 새 알림 요소 생성
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
    
    // 알림 닫기 버튼 이벤트
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', () => {
        notification.classList.add('hide');
        setTimeout(() => {
            notification.remove();
        }, 300);
    });
    
    // 알림 자동 제거 타이머
    setTimeout(() => {
        if (document.body.contains(notification)) {
            notification.classList.add('hide');
            setTimeout(() => {
                if (document.body.contains(notification)) {
                    notification.remove();
                }
            }, 300);
        }
    }, 4000);
    
    // 문서에 알림 요소 추가
    document.body.appendChild(notification);
    
    // 애니메이션을 위한 타이밍
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
}

/**
 * URL 파라미터 가져오기
 * @param {string} name - 파라미터 이름
 * @returns {string|null} 파라미터 값 또는 null
 */
function getUrlParameter(name) {
    const url = window.location.search;
    const urlParams = new URLSearchParams(url);
    return urlParams.get(name);
}